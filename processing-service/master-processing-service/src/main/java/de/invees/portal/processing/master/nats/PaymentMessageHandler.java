package de.invees.portal.processing.master.nats;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.InvoiceDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.OrderDataSourceV1;
import de.invees.portal.common.model.v1.invoice.InvoiceV1;
import de.invees.portal.common.model.v1.order.OrderV1;
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
    List<OrderV1> orders = new ArrayList<>();
    for (long order : invoice.getOrderList()) {
      orders.add(orderDataSource().byId(order, OrderV1.class));
    }
    for (OrderV1 order : orders) {
      workerRegistry.process(order);
    }
  }

  private InvoiceDataSourceV1 invoiceDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(InvoiceDataSourceV1.class);
  }

  public OrderDataSourceV1 orderDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(OrderDataSourceV1.class);
  }
}
