package de.invees.portal.processing.worker.nats;

import de.invees.portal.common.nats.MessageHandler;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.message.processing.Message;
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
    }
  }

  private void execHandle(ExecuteCommandMessage message) {
    ProviderRegistry.access(ServiceProvider.class).execute(message.getCommand());
  }

}
