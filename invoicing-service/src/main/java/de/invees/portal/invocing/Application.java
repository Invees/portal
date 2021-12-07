package de.invees.portal.invocing;

import com.mongodb.client.model.Filters;
import de.invees.portal.common.BasicApplication;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.InvoiceDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.ContractDataSourceV1;
import de.invees.portal.common.model.v1.invoice.InvoiceV1;
import de.invees.portal.common.model.v1.contract.ContractStatusV1;
import de.invees.portal.common.model.v1.contract.ContractV1;
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
        List<ContractV1> orders = contractDataSource()
            .getCollection()
            .find(Filters.eq(ContractV1.STATUS, ContractStatusV1.ACTIVE.toString()))
            .map(d -> contractDataSource().map(d, ContractV1.class))
            .into(new ArrayList<>());
        List<ContractV1> newInvoicesForContract = new ArrayList<>();
        for (ContractV1 order : orders) {
          if (order.getStatus() != ContractStatusV1.ACTIVE) {
            continue;
          }
          if (System.currentTimeMillis() > InvoiceUtils.getNextPaymentDate(order)) {
            newInvoicesForContract.add(order);
          }
        }
        Map<UUID, List<ContractV1>> userOrderMap = new HashMap<>();
        for (ContractV1 order : newInvoicesForContract) {
          if (!userOrderMap.containsKey(order.getBelongsTo())) {
            List<ContractV1> userOrders = new ArrayList<>();
            userOrders.add(order);
            userOrderMap.put(order.getBelongsTo(), userOrders);
          } else {
            userOrderMap.get(order.getBelongsTo()).add(order);
          }
        }
        for (Map.Entry<UUID, List<ContractV1>> entry : userOrderMap.entrySet()) {
          InvoiceV1 invoice = InvoiceUtils.createByContracts(entry.getKey(), entry.getValue());
          for (ContractV1 contract : entry.getValue()) {
            invoice.getContractList().add(contract.getId());
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

  private ContractDataSourceV1 contractDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ContractDataSourceV1.class);
  }

  private InvoiceDataSourceV1 invoiceDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(InvoiceDataSourceV1.class);
  }

}
