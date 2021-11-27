package de.invees.portal.core.service;

import de.invees.portal.common.model.v1.service.console.ServiceConsoleV1;
import de.invees.portal.common.model.v1.service.status.ServiceStatusV1;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.status.CreateConsoleMessage;
import de.invees.portal.common.utils.provider.Provider;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
public class ServiceProvider implements Provider {

  private final Map<UUID, ServiceStatusV1> statusMap = new HashMap<>();
  private final Map<UUID, ServiceConsoleV1> waitingConsole = new HashMap<>();
  private final NatsProvider natsProvider;

  public void apply(ServiceStatusV1 status) {
    statusMap.put(status.getService(), status);
  }

  public ServiceStatusV1 getStatus(UUID service) {
    return statusMap.get(service);
  }

  public void resolveConsole(UUID requestId, ServiceConsoleV1 console) {
    this.waitingConsole.put(requestId, console);
  }

  public ServiceConsoleV1 createConsole(UUID service) {
    UUID requestId = UUID.randomUUID();
    natsProvider.send(Subject.STATUS, new CreateConsoleMessage(requestId, service));
    long iterations = 0;
    while (waitingConsole.get(requestId) == null) {
      try {
        Thread.sleep(1);
      } catch (Exception e) {
        // IGNORE
      }
      if (iterations >= 10000) {
        return null;
      }
      iterations++;
    }
    return waitingConsole.remove(requestId);
  }
}