package de.invees.portal.processing.worker.nats;

import de.invees.portal.common.model.worker.ProcessingWorker;
import de.invees.portal.common.nats.MessageHandler;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.HandshakeMessage;
import de.invees.portal.common.nats.message.processing.MasterStartedMessage;
import de.invees.portal.common.nats.message.processing.Message;
import de.invees.portal.common.nats.message.processing.WelcomeMessage;
import de.invees.portal.processing.worker.Application;
import lombok.Data;

@Data
public class ProcessingMessageHandler implements MessageHandler {

  private final Application application;
  private final NatsProvider natsProvider;

  @Override
  public void handle(Message message) {
    if (message instanceof WelcomeMessage) {
      execHandle((WelcomeMessage) message);
    } else if (message instanceof MasterStartedMessage) {
      execHandle((MasterStartedMessage) message);
    }
  }

  private void execHandle(WelcomeMessage message) {
    if (message.getId().equals(application.getConfiguration().getId())) {
      application.postInitialize();
    }
  }

  private void execHandle(MasterStartedMessage message) {
    Application.LOGGER.info("Found new master - Handshake!");
    natsProvider.send(Subject.PROCESSING, new HandshakeMessage(
        new ProcessingWorker(application.getConfiguration().getId(), application.getConfiguration().getServiceType())
    ));
  }
}
