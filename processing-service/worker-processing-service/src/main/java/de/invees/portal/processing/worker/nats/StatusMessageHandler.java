package de.invees.portal.processing.worker.nats;

import de.invees.portal.common.model.v1.service.command.CommandResponseV1;
import de.invees.portal.common.model.v1.service.console.ServiceConsoleV1;
import de.invees.portal.common.nats.MessageHandler;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.Message;
import de.invees.portal.common.nats.message.status.CommandResponseMessage;
import de.invees.portal.common.nats.message.status.ConsoleResponseMessage;
import de.invees.portal.common.nats.message.status.CreateConsoleMessage;
import de.invees.portal.common.nats.message.status.ExecuteCommandMessage;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.processing.worker.Application;
import de.invees.portal.processing.worker.service.provider.ServiceProvider;
import lombok.Data;

@Data
public class StatusMessageHandler implements MessageHandler {

  private final Application application;
  private final NatsProvider natsProvider;

  @Override
  public void handle(Message message) {
    if (message instanceof ExecuteCommandMessage) {
      execHandle((ExecuteCommandMessage) message);
    } else if (message instanceof CreateConsoleMessage) {
      execHandle((CreateConsoleMessage) message);
    }
  }

  private void execHandle(ExecuteCommandMessage message) {
    CommandResponseV1 response = ProviderRegistry.access(ServiceProvider.class).execute(message.getCommand());
    if (response == null) {
      return;
    }
    natsProvider.send(Subject.STATUS, new CommandResponseMessage(message.getRequestId(), response));
  }

  private void execHandle(CreateConsoleMessage message) {
    ServiceConsoleV1 console = ProviderRegistry.access(ServiceProvider.class).createConsole(message.getServiceId());
    if (console != null) {
      natsProvider.send(Subject.STATUS, new ConsoleResponseMessage(message.getRequestId(), console));
    }
  }
}
