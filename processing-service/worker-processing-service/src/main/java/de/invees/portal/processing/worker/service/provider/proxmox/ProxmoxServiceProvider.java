package de.invees.portal.processing.worker.service.provider.proxmox;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.ProductDataSourceV1;
import de.invees.portal.common.model.v1.order.OrderV1;
import de.invees.portal.common.model.v1.order.request.OrderRequestV1;
import de.invees.portal.common.model.v1.product.ProductV1;
import de.invees.portal.common.model.v1.service.command.CommandV1;
import de.invees.portal.common.model.v1.service.command.CommandResponseV1;
import de.invees.portal.common.model.v1.service.command.ProxmoxActionV1;
import de.invees.portal.common.model.v1.service.console.ServiceConsoleV1;
import de.invees.portal.common.model.v1.service.status.ServiceStatusV1;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.ServiceCreatedMessage;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.processing.worker.Application;
import de.invees.portal.processing.worker.configuration.Configuration;
import de.invees.portal.processing.worker.service.provider.ServiceProvider;
import de.invees.portal.processing.worker.service.provider.proxmox.model.Storage;
import de.invees.portal.processing.worker.service.provider.proxmox.model.VirtualMachine;
import de.invees.portal.processing.worker.service.provider.proxmox.model.VirtualMachineCreate;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProxmoxServiceProvider implements ServiceProvider {

  private Configuration configuration;
  private NatsProvider natsProvider;
  @Getter
  private PveClient pveClient;

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
      int storage = ((Number) product.getFieldList().get("cpu").getValue()).intValue();
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
  public CommandResponseV1 execute(CommandV1 command) {
    if (pveClient.getMachine(command.getService()) == null) {
      return new CommandResponseV1(command.getId(), false);
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
    return new CommandResponseV1(command.getId(), false);
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

  private CommandResponseV1 execHandleStart(CommandV1 command) {
    pveClient.start(command.getService());
    return new CommandResponseV1(command.getId(), true);
  }

  private CommandResponseV1 execHandleRestart(CommandV1 command) {
    pveClient.restart(command.getService());
    return new CommandResponseV1(command.getId(), true);
  }

  private CommandResponseV1 execHandleStop(CommandV1 command) {
    pveClient.stop(command.getService());
    return new CommandResponseV1(command.getId(), true);
  }

  private CommandResponseV1 execHandleKill(CommandV1 command) {
    pveClient.kill(command.getService());
    return new CommandResponseV1(command.getId(), true);
  }

  private String storage() {
    List<Storage> storages = pveClient.getStorages();
    String bestStorage = null;
    double bestUsage = Double.MAX_VALUE;
    for (Storage storage : storages) {
      if (!storage.getStorage().startsWith("disk") && !storage.getStorage().startsWith("storage")) {
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
