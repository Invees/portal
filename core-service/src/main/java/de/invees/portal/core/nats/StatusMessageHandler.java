package de.invees.portal.core.nats;

import de.invees.portal.common.model.v1.service.status.ServiceStatusV1;
import de.invees.portal.common.nats.MessageHandler;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.message.Message;
import de.invees.portal.common.nats.message.status.CommandResponseMessage;
import de.invees.portal.common.nats.message.status.ConsoleResponseMessage;
import de.invees.portal.common.nats.message.status.ServiceStatusMessage;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.core.Application;
import de.invees.portal.core.service.ServiceProvider;
import lombok.Data;

@Data
public class StatusMessageHandler implements MessageHandler {

  private final Application application;
  private final NatsProvider natsProvider;

  @Override
  public void handle(Message message) {
    if (message instanceof ServiceStatusMessage) {
      execHandle((ServiceStatusMessage) message);
    } else if (message instanceof ConsoleResponseMessage) {
      execHandle((ConsoleResponseMessage) message);
    } else if (message instanceof CommandResponseMessage) {
      execHandle((CommandResponseMessage) message);
    }
  }

  private void execHandle(ServiceStatusMessage message) {
    for (ServiceStatusV1 status : message.getStatus()) {
      ProviderRegistry.access(ServiceProvider.class).apply(status);
    }
  }

  private void execHandle(ConsoleResponseMessage message) {
    ProviderRegistry.access(ServiceProvider.class).resolveConsole(message.getRequestId(), message.getConsole());
  }

  private void execHandle(CommandResponseMessage message) {
    ProviderRegistry.access(ServiceProvider.class).resolveCommand(message.getRequestId(), message.getResponse());
  }
}
