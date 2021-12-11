package de.invees.portal.invocing;

import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.*;
import de.invees.portal.common.model.v1.contract.ContractStatusV1;
import de.invees.portal.common.model.v1.contract.ContractV1;
import de.invees.portal.common.model.v1.contract.cancellation.ContractCancellationV1;
import de.invees.portal.common.model.v1.invoice.InvoiceV1;
import de.invees.portal.common.model.v1.service.ServiceV1;
import de.invees.portal.common.model.v1.service.command.CommandV1;
import de.invees.portal.common.model.v1.service.command.ProxmoxActionV1;
import de.invees.portal.common.model.v1.service.network.NetworkAddressV1;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.status.ExecuteCommandMessage;
import de.invees.portal.common.utils.invoice.ContractUtils;
import de.invees.portal.common.utils.provider.ProviderRegistry;

import java.util.*;

public class InvoicingProvider {

  public InvoicingProvider() {
    while (true) {
      try {
        Thread.sleep(5000);
        List<ContractV1> orders = contractDataSource()
            .getCollection()
            .find(Filters.eq(ContractV1.STATUS, ContractStatusV1.ACTIVE.toString()))
            .map(d -> contractDataSource().map(d, ContractV1.class))
            .into(new ArrayList<>());
        List<ContractV1> activeContracts = new ArrayList<>();
        List<ContractV1> canceledContracts = new ArrayList<>();
        for (ContractV1 order : orders) {
          ContractCancellationV1 cancellation = contractCancellationDataSource().getLastCancellation(order.getId());
          if (order.getStatus() != ContractStatusV1.ACTIVE) {
            continue;
          }
          if (System.currentTimeMillis() > ContractUtils.getNextPaymentDate(order)) {
            if (cancellation != null && cancellation.getEffectiveAt() < System.currentTimeMillis()) {
              canceledContracts.add(order); // delete service because the contract is in cancellation
            } else {
              activeContracts.add(order); // create new invoice because the contract is still active
            }
          }
        }
        createInvoices(activeContracts);
        cancelContracts(canceledContracts);
      } catch (Exception e) {
        Application.LOGGER.error("", e);
      }
    }
  }

  private void cancelContracts(List<ContractV1> contracts) {
    for (ContractV1 contract : contracts) {
      ServiceV1 service = serviceDataSourceV1()
          .getCollection()
          .find(Filters.eq(ServiceV1.CONTRACT, contract.getId()))
          .map(d -> serviceDataSourceV1().map(d, ServiceV1.class))
          .first();
      service.setDeleted(true);
      ProviderRegistry.access(NatsProvider.class)
          .send(Subject.STATUS, new ExecuteCommandMessage(
              UUID.randomUUID(),
              new CommandV1(
                  UUID.randomUUID(),
                  null,
                  service.getId(),
                  ProxmoxActionV1.KILL,
                  new HashMap<>()
              )
          ));
      List<NetworkAddressV1> addressList = networkAddressDataSourceV1().getAddressesOfService(service.getId());
      for (NetworkAddressV1 address : addressList) {
        address.setService(null);
        networkAddressDataSourceV1().getCollection().replaceOne(
            Filters.eq(NetworkAddressV1.ID, address.getId().toString()),
            networkAddressDataSourceV1().map(address)
        );
      }

      contract.setStatus(ContractStatusV1.COMPLETED);
      contractDataSource().update(contract);
      serviceDataSourceV1().getCollection().replaceOne(
          Filters.eq(ServiceV1.ID, service.getId().toString()),
          serviceDataSourceV1().map(service)
      );
      Application.LOGGER.info("Contract " + contract.getId() + " was deleted successfully!");
    }
  }

  private void createInvoices(List<ContractV1> contracts) {
    try {
      Map<UUID, List<ContractV1>> userOrderMap = new HashMap<>();
      for (ContractV1 order : contracts) {
        try {
          if (!userOrderMap.containsKey(order.getBelongsTo())) {
            List<ContractV1> userOrders = new ArrayList<>();
            userOrders.add(order);
            userOrderMap.put(order.getBelongsTo(), userOrders);
          } else {
            userOrderMap.get(order.getBelongsTo()).add(order);
          }
        } catch (Exception e) {
          Application.LOGGER.error("", e);
        }
      }
      for (Map.Entry<UUID, List<ContractV1>> entry : userOrderMap.entrySet()) {
        try {
          this.createInvoice(entry);
        } catch (Exception e) {
          Application.LOGGER.error("", e);
        }
      }
    } catch (Exception e) {
      Application.LOGGER.error("", e);
    }
  }

  private void createInvoice(Map.Entry<UUID, List<ContractV1>> entry) {
    try {
      InvoiceV1 invoice = ContractUtils.createByContracts(entry.getKey(), entry.getValue());
      for (ContractV1 contract : entry.getValue()) {
        invoice.getContractList().add(contract.getId());
      }
      invoiceDataSource().create(invoice);
      Application.LOGGER.info("Invoice " + invoice.getId() + " was created successfully!");
    } catch (Exception e) {
      Application.LOGGER.error("Error while creating invoice: ", e);
    }
  }

  private NetworkAddressDataSourceV1 networkAddressDataSourceV1() {
    return ProviderRegistry.access(DataSourceProvider.class).access(NetworkAddressDataSourceV1.class);
  }

  private ServiceDataSourceV1 serviceDataSourceV1() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ServiceDataSourceV1.class);
  }

  private ContractDataSourceV1 contractDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ContractDataSourceV1.class);
  }

  private ContractCancellationDataSourceV1 contractCancellationDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ContractCancellationDataSourceV1.class);
  }

  private InvoiceDataSourceV1 invoiceDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(InvoiceDataSourceV1.class);
  }

}
