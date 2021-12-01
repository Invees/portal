package de.invees.portal.processing.worker.service.provider.proxmox;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.ProductDataSourceV1;
import de.invees.portal.common.model.v1.order.OrderV1;
import de.invees.portal.common.model.v1.order.request.OrderRequestV1;
import de.invees.portal.common.model.v1.product.ProductV1;
import de.invees.portal.common.model.v1.service.command.CommandV1;
import de.invees.portal.common.model.v1.service.command.ProxmoxActionV1;
import de.invees.portal.common.model.v1.service.console.ServiceConsoleV1;
import de.invees.portal.common.model.v1.service.status.ServiceStatusTypeV1;
import de.invees.portal.common.model.v1.service.status.ServiceStatusV1;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.ServiceCreatedMessage;
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

public class ProxmoxServiceProvider implements ServiceProvider {

  private Configuration configuration;
  private NatsProvider natsProvider;
  @Getter
  private PveClient pveClient;
  private final Map<UUID, ServiceStatusTypeV1> overrideStatus = new HashMap<>();

  public ProxmoxServiceProvider(Configuration configuration) {
    this.configuration = configuration;
    this.pveClient = new PveClient(configuration.getProxmox());
    this.natsProvider = ProviderRegistry.access(NatsProvider.class);
  }

  @Override
  public void create(OrderV1 order) {
    try {
      UUID serviceId = UUID.randomUUID();
      OrderRequestV1 request = order.getRequest();
      ProductV1 product = productDataSource().byId(request.getProduct(), ProductV1.class);
      int storage = ((Number) product.getFieldList().get("storage").getValue()).intValue();
      VirtualMachineCreate create = VirtualMachineCreate.builder()
          .vmid(pveClient.getNextId())
          .name(serviceId.toString())
          .memory(((Number) product.getFieldList().get("memory").getValue()).intValue())
          .cores(((Number) product.getFieldList().get("cpu").getValue()).intValue())
          .sata0(storage() + ":" + storage + ",format=qcow2")
          .net0("virtio,bridge=vmbr0,firewall=1")
          .vga("qxl")
          .build();
      pveClient.createVirtualMachine(create);
      while (true) {
        Thread.sleep(2000);
        VirtualMachine machine = pveClient.getMachine(serviceId);
        if (machine != null) {
          break;
        }
      }
      this.natsProvider.send(Subject.PROCESSING, new ServiceCreatedMessage(
          order.getId(),
          serviceId,
          configuration.getId()
      ));
    } catch (Exception e) {
      Application.LOGGER.error("Error while processing order", e);
    }
  }

  @Override
  public void execute(CommandV1 command) {
    if (overrideStatus.get(command.getService()) == ServiceStatusTypeV1.LOCKED) {
      return;
    }
    if (overrideStatus.get(command.getService()) == ServiceStatusTypeV1.INSTALLING) {
      return;
    }
    if (pveClient.getMachine(command.getService()) == null) {
      return;
    }
    if (command.getAction().equals(ProxmoxActionV1.START)) {
      execHandleStart(command);
    }
    if (command.getAction().equals(ProxmoxActionV1.RESTART)) {
      execHandleRestart(command);
    }
    if (command.getAction().equals(ProxmoxActionV1.STOP)) {
      execHandleStop(command);
    }
    if (command.getAction().equals(ProxmoxActionV1.KILL)) {
      execHandleKill(command);
    }
    if (command.getAction().equals(ProxmoxActionV1.INSTALL)) {
      executeHandleInstall(command);
    }
    if (command.getAction().equals(ProxmoxActionV1.MOUNT)) {
      executeMountIso(command);
    }
  }

  private void executeHandleInstall(CommandV1 command) {
    String isoName = (String) command.getData().get("iso");
    String password = (String) command.getData().get("password");
    String hostname = (String) command.getData().get("hostname");
    if (isoName == null || password == null || hostname == null) {
      return;
    }
    new Thread(() -> {
      try {
        overrideStatus.put(command.getService(), ServiceStatusTypeV1.INSTALLING);
        pveClient.kill(command.getService());
        Thread.sleep(1000); // Wait for stop
        File iso = new File("tmp/" + UUID.randomUUID() + ".iso");
        File exportIso = new File("tmp/" + UUID.randomUUID() + ".iso");
        Files.copy(new File(configuration.getNfsDirectory(), "/template/iso/" + isoName).toPath(), iso.toPath());
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
        linesPreseed = linesPreseed.replace("{$password}", password).replace("{$hostname}", hostname);

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
          if (pveClient.getStatus(command.getService()).getStatus() == ServiceStatusTypeV1.STOPPED) {
            break;
          }
          Thread.sleep(30000);
        }

        overrideStatus.remove(command.getService());
      } catch (Exception e) {
        Application.LOGGER.error("", e);
      }
    }).start();
  }

  private void executeMountIso(CommandV1 command) {
    overrideStatus.put(command.getService(), ServiceStatusTypeV1.LOCKED);
    String iso = (String) command.getData().get("iso");
    mount(command.getService(), iso);
    overrideStatus.remove(command.getService());
  }

  private void mount(UUID service, String isoName) {
    try {
      pveClient.mount(service, configuration.getNfsStorage() + ":iso/" + isoName);
    } catch (Exception e) {
      Application.LOGGER.error("", e);
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
      statuses.add(status);
    }
    return statuses;
  }

  @Override
  public ServiceConsoleV1 createConsole(UUID service) {
    if (pveClient.getMachine(service) == null) {
      return null;
    }
    return pveClient.createConsole(service);
  }

  private void execHandleStart(CommandV1 command) {
    pveClient.start(command.getService());
  }

  private void execHandleRestart(CommandV1 command) {
    pveClient.restart(command.getService());
  }

  private void execHandleStop(CommandV1 command) {
    pveClient.stop(command.getService());
  }

  private void execHandleKill(CommandV1 command) {
    pveClient.kill(command.getService());
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

}
