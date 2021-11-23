package de.invees.portal.processing.worker.service.provider.proxmox;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.ProductDataSource;
import de.invees.portal.common.model.order.Order;
import de.invees.portal.common.model.order.request.OrderRequest;
import de.invees.portal.common.model.product.Product;
import de.invees.portal.common.model.service.command.Command;
import de.invees.portal.common.model.service.command.CommandResponse;
import de.invees.portal.common.model.service.command.ProxmoxAction;
import de.invees.portal.common.model.service.console.ServiceConsole;
import de.invees.portal.common.model.service.status.ServiceStatus;
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
  public void create(Order order) {
    try {
      UUID serviceId = UUID.randomUUID();

      OrderRequest request = order.getRequest();
      Product product = productDataSource().byId(request.getProduct(), Product.class);
      VirtualMachineCreate create = VirtualMachineCreate.builder()
          .vmid(pveClient.getNextId())
          .name(serviceId.toString())
          .memory(((Number) product.getFieldList().get("memory").getValue()).intValue())
          .cores(((Number) product.getFieldList().get("cpu").getValue()).intValue())
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
  public CommandResponse execute(Command command) {
    if (command.getAction().equals(ProxmoxAction.START)) {
      return execHandleStart(command);
    }
    if (command.getAction().equals(ProxmoxAction.RESTART)) {
      return execHandleRestart(command);
    }
    if (command.getAction().equals(ProxmoxAction.STOP)) {
      return execHandleStop(command);
    }
    if (command.getAction().equals(ProxmoxAction.KILL)) {
      return execHandleKill(command);
    }
    return new CommandResponse(command.getId(), false);
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
  public List<ServiceStatus> getAllServiceStatus() {
    List<VirtualMachine> machines = pveClient.getVirtualMachines();
    List<ServiceStatus> statuses = new ArrayList<>();
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
      ServiceStatus status = pveClient.getStatus(serviceId);
      statuses.add(status);
    }
    return statuses;
  }

  @Override
  public ServiceConsole createConsole(UUID service) {
    return pveClient.createConsole(service);
  }

  private CommandResponse execHandleStart(Command command) {
    pveClient.start(command.getService());
    return new CommandResponse(command.getId(), true);
  }

  private CommandResponse execHandleRestart(Command command) {
    pveClient.restart(command.getService());
    return new CommandResponse(command.getId(), true);
  }

  private CommandResponse execHandleStop(Command command) {
    pveClient.stop(command.getService());
    return new CommandResponse(command.getId(), true);
  }

  private CommandResponse execHandleKill(Command command) {
    pveClient.kill(command.getService());
    return new CommandResponse(command.getId(), true);
  }

  private String storage() {
    List<Storage> storages = pveClient.getStorages();
    String bestStorage = null;
    double bestUsage = Double.MAX_VALUE;
    for (Storage storage : storages) {
      if (!storage.getName().startsWith("disk") && !storage.getName().startsWith("storage")) {
        continue;
      }
      double usage = storage.getUsed();
      if (usage < bestUsage) {
        bestStorage = storage.getName();
        bestUsage = usage;
      }
    }
    return bestStorage;
  }

  private ProductDataSource productDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ProductDataSource.class);
  }
}
