package de.invees.portal.processing.master.nats;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.OrderDataSource;
import de.invees.portal.common.datasource.mongodb.ProductDataSource;
import de.invees.portal.common.datasource.mongodb.ServiceDataSource;
import de.invees.portal.common.model.order.Order;
import de.invees.portal.common.model.product.Product;
import de.invees.portal.common.model.service.Service;
import de.invees.portal.common.nats.MessageHandler;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.*;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.processing.master.Application;
import de.invees.portal.processing.master.worker.WorkerRegistryProvider;
import lombok.Data;

@Data
public class ProcessingMessageHandler implements MessageHandler {

  private final WorkerRegistryProvider workerRegistry;
  private final NatsProvider natsProvider;

  @Override
  public void handle(Message message) {
    if (message instanceof HandshakeMessage) {
      execHandle((HandshakeMessage) message);
    } else if (message instanceof KeepAliveMessage) {
      execHandle((KeepAliveMessage) message);
    } else if (message instanceof ServiceCreatedMessage) {
      execHandle((ServiceCreatedMessage) message);
    }
  }

  private void execHandle(ServiceCreatedMessage message) {
    Application.LOGGER.info("Service with id '" + message.getServiceId() + "' was created on '" + message.getWorkerId() + "'!");
    Order order = orderDataSource().byId(message.getOrderId(), Order.class);
    Product product = productDataSource().byId(order.getRequest().getProductId(), Product.class);

    serviceDataSource().create(new Service(
        message.getServiceId(),
        order.getUserId(),
        order.getId(),
        message.getWorkerId(),
        product.getType()
    ));
  }

  private void execHandle(HandshakeMessage message) {
    Application.LOGGER.info("Worker with id '" + message.getWorker().getId() + "' joined the processing cluster.");
    workerRegistry.register(message.getWorker());
    natsProvider.send(Subject.PROCESSING, new WelcomeMessage(message.getWorker().getId()));
  }

  private void execHandle(KeepAliveMessage message) {
    workerRegistry.keepAlive(message.getId(), message.getUsage());
  }

  public OrderDataSource orderDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(OrderDataSource.class);
  }

  public ProductDataSource productDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ProductDataSource.class);
  }

  public ServiceDataSource serviceDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ServiceDataSource.class);
  }
}
