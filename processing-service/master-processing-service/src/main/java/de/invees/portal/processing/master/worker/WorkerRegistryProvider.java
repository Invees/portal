package de.invees.portal.processing.master.worker;

import de.invees.portal.common.model.order.Order;
import de.invees.portal.common.model.service.ServiceType;
import de.invees.portal.common.model.worker.ProcessingWorker;
import de.invees.portal.common.utils.provider.Provider;
import de.invees.portal.processing.master.Application;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerRegistryProvider implements Provider {

  private final Map<ServiceType, Map<UUID, ProcessingWorker>> workerMap = new ConcurrentHashMap<>();
  private final Map<UUID, Long> keepAliveList = new ConcurrentHashMap<>();

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
    // FIND BEST HOST SYSTEM AND DEPLOY IT!
  }

  public void register(ProcessingWorker worker) {
    Map<UUID, ProcessingWorker> workers = getWorkersForType(worker.getServiceType());
    workers.put(worker.getId(), worker);
    keepAlive(worker.getId());
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
      return new HashMap<>();
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

  public void keepAlive(UUID id) {
    keepAliveList.put(id, System.currentTimeMillis() + 5000);
  }
}
