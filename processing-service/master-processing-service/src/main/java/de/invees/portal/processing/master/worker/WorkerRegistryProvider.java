package de.invees.portal.processing.master.worker;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.ProductDataSourceV1;
import de.invees.portal.common.model.v1.order.OrderV1;
import de.invees.portal.common.model.v1.product.ProductV1;
import de.invees.portal.common.model.v1.service.ServiceTypeV1;
import de.invees.portal.common.model.v1.worker.ProcessingWorkerV1;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.ExecuteOrderMessage;
import de.invees.portal.common.utils.provider.Provider;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.processing.master.Application;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerRegistryProvider implements Provider {

  private final Map<ServiceTypeV1, Map<UUID, ProcessingWorkerV1>> workerMap = new ConcurrentHashMap<>();
  private final Map<UUID, Long> keepAliveList = new ConcurrentHashMap<>();
  private final Map<UUID, Double> usageMap = new ConcurrentHashMap<>();

  public WorkerRegistryProvider() {
    new Thread(() -> {
      while (true) {
        try {
          Thread.sleep(10000);
          List<UUID> disconnectedWorker = new ArrayList<>();
          for (Map.Entry<UUID, Long> entry : keepAliveList.entrySet()) {
            if (entry.getValue() < System.currentTimeMillis()) {
              disconnectedWorker.add(entry.getKey());
            }
          }
          for (UUID worker : disconnectedWorker) {
            Application.LOGGER.info("Worker with id '" + worker + "' lost connection.");
            this.remove(worker);
          }
        } catch (Exception e) {
          Application.LOGGER.error("Error while keep alive check", e);
        }
      }
    }).start();
  }

  public void process(OrderV1 order) {
    ProductV1 product = productDataSource().byId(order.getRequest().getProduct(), ProductV1.class);
    Map<UUID, ProcessingWorkerV1> workers = workerMap.get(product.getType());
    UUID bestWorker = null;
    double bestUsage = 101;
    for (Map.Entry<UUID, ProcessingWorkerV1> entry : workers.entrySet()) {
      if (!usageMap.containsKey(entry.getKey())) {
        continue;
      }
      double usage = usageMap.get(entry.getKey());
      if (usage < bestUsage) {
        bestWorker = entry.getKey();
        bestUsage = usage;
      }
    }
    ProviderRegistry.access(NatsProvider.class).send(Subject.PROCESSING, new ExecuteOrderMessage(bestWorker, order));
  }

  public void register(ProcessingWorkerV1 worker) {
    Map<UUID, ProcessingWorkerV1> workers = getWorkersForType(worker.getServiceType());
    workers.put(worker.getId(), worker);
  }

  public void remove(UUID id) {
    keepAliveList.remove(id);
    for (ServiceTypeV1 type : ServiceTypeV1.values()) {
      getWorkersForType(type).remove(id);
    }
  }

  public Map<UUID, ProcessingWorkerV1> getWorkersForType(ServiceTypeV1 type) {
    Map<UUID, ProcessingWorkerV1> workers = workerMap.get(type);
    if (workers == null) {
      workers = new HashMap<>();
      workerMap.put(type, workers);
    }
    return workers;
  }

  public ProcessingWorkerV1 byId(UUID id) {
    for (ServiceTypeV1 type : ServiceTypeV1.values()) {
      Map<UUID, ProcessingWorkerV1> entry = getWorkersForType(type);
      ProcessingWorkerV1 worker = entry.get(id);
      if (worker != null) {
        return worker;
      }
    }
    return null;
  }

  public void keepAlive(UUID id, double usage) {
    keepAliveList.put(id, System.currentTimeMillis() + 5000);
    usageMap.put(id, usage);
  }

  private ProductDataSourceV1 productDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ProductDataSourceV1.class);
  }
}
