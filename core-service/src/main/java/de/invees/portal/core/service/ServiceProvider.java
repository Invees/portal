package de.invees.portal.core.service;

import de.invees.portal.common.model.service.status.ServiceStatus;
import de.invees.portal.common.utils.provider.Provider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServiceProvider implements Provider {

  private Map<UUID, ServiceStatus> statusMap = new HashMap<>();

  public void apply(ServiceStatus status) {
    statusMap.put(status.getService(), status);
  }

  public ServiceStatus getStatus(UUID service) {
    return statusMap.get(service);
  }

}
