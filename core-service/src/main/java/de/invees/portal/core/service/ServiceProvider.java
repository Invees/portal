package de.invees.portal.core.service;

import de.invees.portal.common.model.service.console.ServiceConsole;
import de.invees.portal.common.model.service.status.ServiceStatus;
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

  private final Map<UUID, ServiceStatus> statusMap = new HashMap<>();
  private final Map<UUID, ServiceConsole> waitingConsole = new HashMap<>();
  private final NatsProvider natsProvider;

  public void apply(ServiceStatus status) {
    statusMap.put(status.getService(), status);
  }

  public ServiceStatus getStatus(UUID service) {
    return statusMap.get(service);
  }

  public void resolveConsole(UUID requestId, ServiceConsole console) {
    this.waitingConsole.put(requestId, console);
  }

  public ServiceConsole createConsole(UUID service) {
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