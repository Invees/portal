package de.invees.portal.processing.worker.service.provider.proxmox;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.NetworkAddressDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.ProductDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.SoftwareDataSourceV1;
import de.invees.portal.common.model.v1.contract.ContractV1;
import de.invees.portal.common.model.v1.order.ContractUpgradeV1;
import de.invees.portal.common.model.v1.order.OrderV1;
import de.invees.portal.common.model.v1.product.ProductV1;
import de.invees.portal.common.model.v1.service.command.CommandResponseV1;
import de.invees.portal.common.model.v1.service.command.CommandV1;
import de.invees.portal.common.model.v1.service.command.ProxmoxActionV1;
import de.invees.portal.common.model.v1.service.console.ServiceConsoleV1;
import de.invees.portal.common.model.v1.service.network.NetworkAddressV1;
import de.invees.portal.common.model.v1.service.software.ServiceSoftwareTypeV1;
import de.invees.portal.common.model.v1.service.software.ServiceSoftwareV1;
import de.invees.portal.common.model.v1.service.status.ServiceStatusTypeV1;
import de.invees.portal.common.model.v1.service.status.ServiceStatusV1;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.ServiceCreatedMessage;
import de.invees.portal.common.utils.IOUtils;
import de.invees.portal.common.utils.process.ProcessUtils;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.processing.worker.Application;
import de.invees.portal.processing.worker.configuration.Configuration;
import de.invees.portal.processing.worker.service.provider.ServiceProvider;
import de.invees.portal.processing.worker.service.provider.proxmox.model.Storage;
import de.invees.portal.processing.worker.service.provider.proxmox.model.VirtualMachine;
import de.invees.portal.processing.worker.service.provider.proxmox.model.VirtualMachineCreate;
import lombok.Getter;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProxmoxServiceProvider implements ServiceProvider {

  private Application application;
  private Configuration configuration;
  private NatsProvider natsProvider;
  @Getter
  private PveClient pveClient;
  private final Map<UUID, ServiceStatusTypeV1> overrideStatus = new HashMap<>();
  ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  public ProxmoxServiceProvider(Application application, Configuration configuration) {
    this.application = application;
    this.configuration = configuration;
    this.pveClient = new PveClient(configuration.getProxmox());
    this.natsProvider = ProviderRegistry.access(NatsProvider.class);
  }

  @Override
  public void create(ContractV1 contract) {
    try {
      UUID service = UUID.randomUUID();
      OrderV1 order = contract.getOrder();
      ProductV1 product = productDataSource().byId(order.getProduct(), ProductV1.class);
      int storage = ((Number) product.getFieldList().get("storage").getValue()).intValue();
      VirtualMachineCreate create = VirtualMachineCreate.builder()
          .vmid(pveClient.getNextId())
          .name(service.toString())
          .memory(((Number) product.getFieldList().get("memory").getValue()).intValue())
          .cores(((Number) product.getFieldList().get("cpu").getValue()).intValue())
          .sata0(storage() + ":" + storage + ",format=qcow2")
          .net0("virtio,bridge=vmbr0,firewall=0")
          .vga("qxl")
          .build();

      pveClient.createVirtualMachine(create);
      while (true) {
        Thread.sleep(2000);
        VirtualMachine machine = pveClient.getMachine(service);
        if (machine != null) {
          break;
        }
      }
      pveClient.enableIpFilter(service);
      this.mount(service, "");
      pveClient.setBootOrder(service, "order=ide2;sata0;net0");
      Thread.sleep(2000);

      applyUpgradeSingle(service, new ContractUpgradeV1("ipv4", 1));

      if (contract.getOrder().getConfiguration().containsKey("ipv4")) {
        applyUpgradeSingle(service, new ContractUpgradeV1("ipv4", contract.getOrder().getConfiguration().get("ipv4")));
      }

      this.natsProvider.send(Subject.PROCESSING, new ServiceCreatedMessage(
          contract.getId(),
          service,
          configuration.getId()
      ));
    } catch (Exception e) {
      Application.LOGGER.error("Error while processing order", e);
    }
  }

  @Override
  public CommandResponseV1 execute(CommandV1 command) {
    if (pveClient.getMachine(command.getService()) == null) {
      return null;
    }
    if (overrideStatus.get(command.getService()) == ServiceStatusTypeV1.LOCKED) {
      return new CommandResponseV1(false, "LOCKED");
    }
    if (overrideStatus.get(command.getService()) == ServiceStatusTypeV1.INSTALLING) {
      return new CommandResponseV1(false, "INSTALLING");
    }
    if (command.getAction().equals(ProxmoxActionV1.START)) {
      return execHandleStart(command);
    }
    if (command.getAction().equals(ProxmoxActionV1.RESTART)) {
      return execHandleRestart(command);
    }
    if (command.getAction().equals(ProxmoxActionV1.STOP)) {
      return execHandleStop(command);
    }
    if (command.getAction().equals(ProxmoxActionV1.KILL)) {
      return execHandleKill(command);
    }
    if (command.getAction().equals(ProxmoxActionV1.INSTALL)) {
      return executeHandleInstall(command);
    }
    if (command.getAction().equals(ProxmoxActionV1.MOUNT)) {
      return executeMountIso(command);
    }
    return null;
  }

  private CommandResponseV1 executeHandleInstall(CommandV1 command) {
    String isoName = (String) command.getData().get("iso");
    String password = (String) command.getData().get("password");
    String hostname = (String) command.getData().get("hostname");
    if (isoName == null || password == null || hostname == null) {
      return new CommandResponseV1(false, "MISSING_ARGUMENT");
    }
    ServiceSoftwareV1 software = softwareDataSource().byId(isoName, ServiceSoftwareV1.class);
    if (software == null || software.getType() != ServiceSoftwareTypeV1.INSTALLATION) {
      return new CommandResponseV1(false, "INSTALLATION_NOT_FOUND");
    }
    String isoId = software.getId().toString() + ".iso";
    NetworkAddressV1 address = networkAddressDataSource().getAddressesOfService(command.getService()).get(0);

    new Thread(() -> {
      try {
        overrideStatus.put(command.getService(), ServiceStatusTypeV1.INSTALLING);
        pveClient.kill(command.getService());
        Thread.sleep(1000); // Wait for stop
        File iso = new File("tmp/" + UUID.randomUUID() + ".iso");
        File exportIso = new File("tmp/" + UUID.randomUUID() + ".iso");
        Files.copy(new File(configuration.getNfsDirectory(), "/template/iso/" + isoId).toPath(), iso.toPath());
        // Unpack the iso
        File isoFiles = new File("tmp/" + UUID.randomUUID());
        isoFiles.mkdirs();
        ProcessUtils.execAndWait("bsdtar -C " + isoFiles.getAbsolutePath() + " -xf " + iso.getAbsolutePath());
        ProcessUtils.execAndWait("chmod -R +w " + isoFiles.getAbsolutePath());
        // Update isolinux.cfg
        File isolinux = new File(isoFiles, "isolinux/isolinux.cfg");
        isolinux.delete();
        String linesIsolinux = Files.readString(new File("install/isolinux.cfg").toPath());

        Files.write(isolinux.toPath(), linesIsolinux.getBytes());
        // Add preseed.cfg
        ProcessUtils.execAndWait("gunzip " + isoFiles.getAbsolutePath() + "/install.amd/initrd.gz");

        File preseed = new File(isoFiles, "preseed.cfg");
        String linesPreseed = Files.readString(new File("install/preseed.cfg").toPath());
        linesPreseed = linesPreseed.replace("{$password}", password)
            .replace("{$hostname}", hostname)
            .replace("{$address}", address.getAddress())
            .replace("{$netmask}", address.getNetmask())
            .replace("{$gateway}", address.getGateway());

        Files.write(preseed.toPath(), linesPreseed.getBytes());
        ProcessUtils.execAndWait("echo preseed.cfg | cpio -H newc -o -A -F install.amd/initrd", isoFiles);

        ProcessUtils.execAndWait("gzip " + isoFiles.getAbsolutePath() + "/install.amd/initrd");

        // Pack Iso
        ProcessUtils.execAndWait("find " + isoFiles.getAbsolutePath() + " -type f -exec md5sum {} \\; > "
            + isoFiles.getAbsolutePath() + "/md5sum.txt");

        ProcessUtils.execAndWait("genisoimage -V Debian-headless \\\n"
            + "        -r -J -b isolinux/isolinux.bin -c isolinux/boot.cat \\\n"
            + "        -no-emul-boot -boot-load-size 4 -boot-info-table \\\n"
            + "        -o " + exportIso.getAbsolutePath() + " " + isoFiles.getAbsolutePath());

        ProcessUtils.execAndWait("isohybrid " + exportIso.getAbsolutePath());

        // Redeploy iso to storage
        File nfsExport = new File(configuration.getNfsDirectory(), "/template/iso/" + exportIso.getName());
        Files.copy(exportIso.toPath(), nfsExport.toPath());

        // Mount ISO to VM
        this.mount(command.getService(), exportIso.getName());

        Thread.sleep(1000);
        // Start VM
        pveClient.start(command.getService());

        // Wait until shutdown
        while (true) {
          Thread.sleep(30000);
          if (pveClient.getStatus(command.getService()).getStatus() == ServiceStatusTypeV1.STOPPED) {
            break;
          }
        }

        this.mount(command.getService(), "");

        IOUtils.delete(iso);
        IOUtils.delete(exportIso);
        IOUtils.delete(nfsExport);
        IOUtils.delete(isoFiles);

        overrideStatus.remove(command.getService());
      } catch (Exception e) {
        Application.LOGGER.error("", e);
      }
    }).start();
    return new CommandResponseV1(true, "");
  }

  private CommandResponseV1 executeMountIso(CommandV1 command) {
    String iso = (String) command.getData().get("iso");
    if (iso == null || iso.equalsIgnoreCase("none") || iso.equalsIgnoreCase("")) {
      mount(command.getService(), "");
      return new CommandResponseV1(true, "");
    }
    ServiceSoftwareV1 software = softwareDataSource().byId(iso, ServiceSoftwareV1.class);
    if (software == null || software.getType() != ServiceSoftwareTypeV1.MOUNTABLE) {
      return new CommandResponseV1(false, "MOUNTABLE_NOT_FOUND");
    }
    if (software.getBelongsTo() != null && !software.getBelongsTo().equals(command.getExecutor())) {
      return new CommandResponseV1(false, "UNAUTHORIZED");
    }
    overrideStatus.put(command.getService(), ServiceStatusTypeV1.LOCKED);
    mount(command.getService(), iso + ".iso");
    overrideStatus.remove(command.getService());
    return new CommandResponseV1(true, "");
  }

  private void mount(UUID service, String isoName) {
    try {
      if (isoName.equals("none") || isoName.equals("") || isoName == null) {
        pveClient.mount(service, "none,media=cdrom,size=378M");
      } else {
        pveClient.mount(service, configuration.getNfsStorage() + ":iso/" + isoName);
      }
      executor.schedule(application::sendStatus, 1000, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      Application.LOGGER.error("", e);
    }
  }

  private void applyUpgradeSingle(UUID service, ContractUpgradeV1 upgrade) {
    if (upgrade.getKey().equalsIgnoreCase("ipv4")) {
      int fullCount = 0;
      if (upgrade.getValue() instanceof Double) {
        fullCount = ((Double) upgrade.getValue()).intValue();
      } else {
        fullCount = (int) upgrade.getValue();
      }
      for (int x = 0; x < fullCount; x++) {
        NetworkAddressV1 address = networkAddressDataSource().applyNextAddress(service);
        pveClient.addAddress(service, address.getAddress());
      }
    }
  }

  @Override
  public double getUsage() {
    List<VirtualMachine> machines = pveClient.getVirtualMachines();
    int usedCores = 0;
    for (VirtualMachine machine : machines) {
      usedCores += machine.getCpus();
    }
    return ((double) usedCores / (double) configuration.getProxmox().getMaxCores()) * 100.0;
  }

  @Override
  public List<ServiceStatusV1> getAllServiceStatus() {
    List<VirtualMachine> machines = pveClient.getVirtualMachines();
    List<ServiceStatusV1> statuses = new ArrayList<>();
    for (VirtualMachine machine : machines) {
      UUID serviceId = null;
      try {
        serviceId = UUID.fromString(machine.getName());
      } catch (IllegalArgumentException e) {
        // WE DO NOTHING
      }
      if (serviceId == null) {
        continue;
      }
      ServiceStatusV1 status = pveClient.getStatus(serviceId);
      if (overrideStatus.containsKey(serviceId)) {
        status.setStatus(overrideStatus.get(serviceId));
      }
      statuses.add(status);
    }
    return statuses;
  }

  @Override
  public ServiceConsoleV1 createConsole(UUID service) {
    if (pveClient.getMachine(service) == null) {
      return null;
    }
    if (overrideStatus.get(service) == ServiceStatusTypeV1.LOCKED) {
      return null;
    }
    if (overrideStatus.get(service) == ServiceStatusTypeV1.INSTALLING) {
      return null;
    }
    return pveClient.createConsole(service);
  }

  @Override
  public void applyUpgrade(UUID service, List<ContractUpgradeV1> upgrades) {
    for (ContractUpgradeV1 upgrade : upgrades) {
      this.applyUpgradeSingle(service, upgrade);
    }
  }

  private CommandResponseV1 execHandleStart(CommandV1 command) {
    pveClient.start(command.getService());
    executor.schedule(application::sendStatus, 1000, TimeUnit.MILLISECONDS);
    return new CommandResponseV1(true, "");
  }

  private CommandResponseV1 execHandleRestart(CommandV1 command) {
    pveClient.restart(command.getService());
    executor.schedule(application::sendStatus, 1000, TimeUnit.MILLISECONDS);
    return new CommandResponseV1(true, "");
  }

  private CommandResponseV1 execHandleStop(CommandV1 command) {
    pveClient.stop(command.getService());
    executor.schedule(application::sendStatus, 1000, TimeUnit.MILLISECONDS);
    return new CommandResponseV1(true, "");
  }

  private CommandResponseV1 execHandleKill(CommandV1 command) {
    pveClient.kill(command.getService());
    executor.schedule(application::sendStatus, 1000, TimeUnit.MILLISECONDS);
    return new CommandResponseV1(true, "");
  }

  private String storage() {
    List<Storage> storages = pveClient.getStorages();
    String bestStorage = null;
    double bestUsage = Double.MAX_VALUE;
    for (Storage storage : storages) {
      if (!storage.getStorage().startsWith(configuration.getDiskPrefix())) {
        continue;
      }
      double usage = storage.getUsed();
      if (usage < bestUsage) {
        bestStorage = storage.getStorage();
        bestUsage = usage;
      }
    }
    return bestStorage;
  }

  private ProductDataSourceV1 productDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ProductDataSourceV1.class);
  }

  private SoftwareDataSourceV1 softwareDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(SoftwareDataSourceV1.class);
  }

  private NetworkAddressDataSourceV1 networkAddressDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(NetworkAddressDataSourceV1.class);
  }
}
