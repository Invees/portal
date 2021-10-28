package de.invees.portal.processing.master.worker;

import de.invees.portal.common.model.service.UserServiceType;
import de.invees.portal.common.model.worker.ProcessingWorker;
import de.invees.portal.common.utils.service.Service;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorkerRegistryService implements Service {

  private final Map<UserServiceType, Map<UUID, ProcessingWorker>> workerMap = new EnumMap<>(UserServiceType.class);

  public void register(ProcessingWorker worker) {
    Map<UUID, ProcessingWorker> workers = getWorkersForType(worker.getServiceType());
    workers.put(worker.getId(), worker);
  }

  public ProcessingWorker remove(UUID id) {
    ProcessingWorker worker = byId(id);
    return this.workerMap.get(worker.getServiceType())
        .remove(worker.getId());
  }

  public Map<UUID, ProcessingWorker> getWorkersForType(UserServiceType type) {
    Map<UUID, ProcessingWorker> workers = workerMap.get(type);
    if (workers == null) {
      return new HashMap<>();
    }
    return workers;
  }

  public ProcessingWorker byId(UUID id) {
    for (UserServiceType type : UserServiceType.values()) {
      Map<UUID, ProcessingWorker> entry = getWorkersForType(type);
      ProcessingWorker worker = entry.get(id);
      if (worker != null) {
        return worker;
      }
    }
    return null;
  }

}
