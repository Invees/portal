package de.invees.portal.processing.master.nats;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.ContractDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.InvoiceDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.ProductDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.ServiceDataSourceV1;
import de.invees.portal.common.model.v1.contract.ContractStatusV1;
import de.invees.portal.common.model.v1.contract.ContractV1;
import de.invees.portal.common.model.v1.product.ProductV1;
import de.invees.portal.common.model.v1.service.ServiceV1;
import de.invees.portal.common.nats.MessageHandler;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.Message;
import de.invees.portal.common.nats.message.processing.HandshakeMessage;
import de.invees.portal.common.nats.message.processing.KeepAliveMessage;
import de.invees.portal.common.nats.message.processing.ServiceCreatedMessage;
import de.invees.portal.common.nats.message.processing.WelcomeMessage;
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
    ContractV1 contract = contractDataSource().byId(message.getContractId(), ContractV1.class);

    ProductV1 product = productDataSource().byId(contract.getOrder().getProduct(), ProductV1.class);
    contract.setStatus(ContractStatusV1.ACTIVE);
    contractDataSource().update(contract);

    serviceDataSource().create(new ServiceV1(
        message.getServiceId(),
        product.getDisplayName().getDe() + "-" + message.getServiceId().toString().split("-")[0],
        contract.getBelongsTo(),
        contract.getId(),
        message.getWorkerId(),
        product.getType(),
        false
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

  public ContractDataSourceV1 contractDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ContractDataSourceV1.class);
  }

  public ProductDataSourceV1 productDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ProductDataSourceV1.class);
  }

  public ServiceDataSourceV1 serviceDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ServiceDataSourceV1.class);
  }

  public InvoiceDataSourceV1 invoiceDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(InvoiceDataSourceV1.class);
  }
}
