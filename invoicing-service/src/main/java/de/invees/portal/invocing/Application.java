package de.invees.portal.invocing;

import com.mongodb.client.model.Filters;
import de.invees.portal.common.BasicApplication;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.InvoiceDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.OrderDataSourceV1;
import de.invees.portal.common.model.v1.invoice.InvoiceV1;
import de.invees.portal.common.model.v1.order.OrderStatusV1;
import de.invees.portal.common.model.v1.order.OrderV1;
import de.invees.portal.common.utils.invoice.InvoiceUtils;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.invocing.configuration.Configuration;

import java.util.*;

public class Application extends BasicApplication {

  private Configuration configuration;

  public static void main(String[] args) {
    new Application();
  }

  private Application() {
    LOGGER.info("Starting Invees/Portal/Invoicing v" + VERSION);
    if (!loadConfiguration()) {
      return;
    }
    loadDataSource(configuration.getDataSource());
    loadNatsProvider(configuration.getNats());
    LOGGER.info("-------- SERVICE STARTED --------");
    while (true) {
      try {
        Thread.sleep(5000);
        List<OrderV1> orders = orderDataSource()
            .getCollection()
            .find(Filters.eq(OrderV1.STATUS, OrderStatusV1.ACTIVE.toString()))
            .map(d -> orderDataSource().map(d, OrderV1.class))
            .into(new ArrayList<>());
        List<OrderV1> newInvoiceOrders = new ArrayList<>();
        for (OrderV1 order : orders) {
          if (order.getStatus() != OrderStatusV1.ACTIVE) {
            continue;
          }
          if (System.currentTimeMillis() > InvoiceUtils.getNextPaymentDate(order)) {
            newInvoiceOrders.add(order);
          }
        }
        Map<UUID, List<OrderV1>> userOrderMap = new HashMap<>();
        for (OrderV1 order : newInvoiceOrders) {
          if (!userOrderMap.containsKey(order.getBelongsTo())) {
            List<OrderV1> userOrders = new ArrayList<>();
            userOrders.add(order);
            userOrderMap.put(order.getBelongsTo(), userOrders);
          } else {
            userOrderMap.get(order.getBelongsTo()).add(order);
          }
        }
        for (Map.Entry<UUID, List<OrderV1>> entry : userOrderMap.entrySet()) {
          InvoiceV1 invoice = InvoiceUtils.createByOrders(entry.getKey(), entry.getValue());
          for (OrderV1 order : entry.getValue()) {
            invoice.getOrderList().add(order.getId());
          }
          invoiceDataSource().create(invoice);
          Application.LOGGER.info("Invoice " + invoice.getId() + " was created successfully!");
        }
      } catch (Exception e) {
        Application.LOGGER.error("", e);
      }
    }
  }

  public boolean loadConfiguration() {
    try {
      LOGGER.info("Loading configuration..");
      this.configuration = this.loadConfiguration(Configuration.class);
      return true;
    } catch (Exception e) {
      LOGGER.warn("Error while reading configuration", e);
      return false;
    }
  }

  private OrderDataSourceV1 orderDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(OrderDataSourceV1.class);
  }

  private InvoiceDataSourceV1 invoiceDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(InvoiceDataSourceV1.class);
  }

}
