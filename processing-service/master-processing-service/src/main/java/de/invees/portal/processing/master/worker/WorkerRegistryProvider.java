package de.invees.portal.processing.master.worker;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.ProductDataSource;
import de.invees.portal.common.model.order.Order;
import de.invees.portal.common.model.product.Product;
import de.invees.portal.common.model.service.ServiceType;
import de.invees.portal.common.model.worker.ProcessingWorker;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.ExecuteOrderMessage;
import de.invees.portal.common.utils.provider.Provider;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.processing.master.Application;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerRegistryProvider implements Provider {

  private final Map<ServiceType, Map<UUID, ProcessingWorker>> workerMap = new ConcurrentHashMap<>();
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

  public void process(Order order) {
    Product product = productDataSource().byId(order.getRequest().getProduct(), Product.class);
    Map<UUID, ProcessingWorker> workers = workerMap.get(product.getType());
    UUID bestWorker = null;
    double bestUsage = 101;
    for (Map.Entry<UUID, ProcessingWorker> entry : workers.entrySet()) {
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

  public void register(ProcessingWorker worker) {
    Map<UUID, ProcessingWorker> workers = getWorkersForType(worker.getServiceType());
    workers.put(worker.getId(), worker);
  }

  public void remove(UUID id) {
    keepAliveList.remove(id);
    for (ServiceType type : ServiceType.values()) {
      getWorkersForType(type).remove(id);
    }
  }

  public Map<UUID, ProcessingWorker> getWorkersForType(ServiceType type) {
    Map<UUID, ProcessingWorker> workers = workerMap.get(type);
    if (workers == null) {
      workers = new HashMap<>();
      workerMap.put(type, workers);
    }
    return workers;
  }

  public ProcessingWorker byId(UUID id) {
    for (ServiceType type : ServiceType.values()) {
      Map<UUID, ProcessingWorker> entry = getWorkersForType(type);
      ProcessingWorker worker = entry.get(id);
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

  private ProductDataSource productDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ProductDataSource.class);
  }
}
