package de.invees.portal.processing.master.nats;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.InvoiceDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.ContractDataSourceV1;
import de.invees.portal.common.model.v1.invoice.InvoiceV1;
import de.invees.portal.common.model.v1.contract.ContractV1;
import de.invees.portal.common.nats.MessageHandler;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.message.Message;
import de.invees.portal.common.nats.message.payment.PaymentMessage;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.processing.master.Application;
import de.invees.portal.processing.master.worker.WorkerRegistryProvider;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PaymentMessageHandler implements MessageHandler {

  private final WorkerRegistryProvider workerRegistry;
  private final Application application;
  private final NatsProvider natsProvider;

  @Override
  public void handle(Message message) {
    if (message instanceof PaymentMessage) {
      execHandle((PaymentMessage) message);
    }
  }

  private void execHandle(PaymentMessage message) {
    InvoiceV1 invoice = invoiceDataSource().byId(message.getInvoiceId(), InvoiceV1.class);
    List<ContractV1> contracts = new ArrayList<>();
    for (long contractId : invoice.getContractList()) {
      contracts.add(contractDataSource().byId(contractId, ContractV1.class));
    }
    for (ContractV1 contract : contracts) {
      workerRegistry.process(contract);
    }
  }

  private InvoiceDataSourceV1 invoiceDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(InvoiceDataSourceV1.class);
  }

  public ContractDataSourceV1 contractDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ContractDataSourceV1.class);
  }
}
