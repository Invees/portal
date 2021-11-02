package de.invees.portal.processing.master.nats;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.InvoiceDataSource;
import de.invees.portal.common.datasource.mongodb.OrderDataSource;
import de.invees.portal.common.model.invoice.Invoice;
import de.invees.portal.common.model.order.Order;
import de.invees.portal.common.model.order.OrderStatus;
import de.invees.portal.common.nats.MessageHandler;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.message.payment.PaymentMessage;
import de.invees.portal.common.nats.message.processing.Message;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.processing.master.Application;
import de.invees.portal.processing.master.worker.WorkerRegistryProvider;
import lombok.Data;

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
    Invoice invoice = invoiceDataSource().byId(message.getInvoiceId(), Invoice.class);
    List<Order> orders = orderDataSource().byInvoiceId(invoice.getId());
    for (Order order : orders) {
      workerRegistry.process(order);
    }
  }

  private InvoiceDataSource invoiceDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(InvoiceDataSource.class);
  }

  public OrderDataSource orderDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(OrderDataSource.class);
  }
}
