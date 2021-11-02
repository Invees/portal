package de.invees.portal.processing.worker.service.provider.proxmox;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.ProductDataSource;
import de.invees.portal.common.model.order.Order;
import de.invees.portal.common.model.order.request.OrderRequest;
import de.invees.portal.common.model.product.Product;
import de.invees.portal.common.model.service.command.Command;
import de.invees.portal.common.model.service.command.CommandResponse;
import de.invees.portal.common.model.service.command.ProxmoxAction;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.processing.worker.Application;
import de.invees.portal.processing.worker.configuration.ProxmoxConfiguration;
import de.invees.portal.processing.worker.service.provider.ServiceProvider;

import java.util.UUID;

public class ProxmoxServiceProvider implements ServiceProvider {

  private PveClient pveClient;

  public ProxmoxServiceProvider(ProxmoxConfiguration configuration) {
    this.pveClient = new PveClient(configuration);
  }

  @Override
  public void create(Order order) {
    OrderRequest request = order.getRequest();
    Product product = productDataSource().byId(request.getProductId(), Product.class);

    try {
      VirtualMachine machine = VirtualMachine.builder()
          .vmid(pveClient.getNextId())
          .name(order.getId().toString())
          .memory(((Number) product.getFieldList().get("memory").getValue()).intValue())
          .cores(((Number) product.getFieldList().get("cpu").getValue()).intValue())
          .build();

      pveClient.createVirtualMachine(machine);
    } catch (Exception e) {
      Application.LOGGER.error("Error while processing order", e);
    }
  }

  @Override
  public CommandResponse execute(Command command) {
    if (command.getAction().equals(ProxmoxAction.START)) {
      return execHandleStart(command);
    }
    return new CommandResponse();
  }

  private CommandResponse execHandleStart(Command command) {
    return null;
  }

  private String disk() {
    return "disk-1";
  }

  private int nextId() {
    // pvesh get /cluster/nextid
    return 105;
  }

  private ProductDataSource productDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ProductDataSource.class);
  }
}
