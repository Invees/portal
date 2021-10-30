package de.invees.portal.processing.worker.nats;

import de.invees.portal.common.model.worker.ProcessingWorker;
import de.invees.portal.common.nats.MessageHandler;
import de.invees.portal.common.nats.NatsService;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.HandshakeMessage;
import de.invees.portal.common.nats.message.processing.MasterStartedMessage;
import de.invees.portal.common.nats.message.processing.Message;
import de.invees.portal.common.nats.message.processing.WelcomeMessage;
import de.invees.portal.processing.worker.Application;
import lombok.Data;

@Data
public class ConnectionMessageHandler implements MessageHandler {

  private final Application application;
  private final NatsService natsService;

  @Override
  public void handle(Message message) {
    if (message instanceof WelcomeMessage) {
      _handle((WelcomeMessage) message);
    } else if (message instanceof MasterStartedMessage) {
      _handle();
    }
  }

  private void _handle(WelcomeMessage message) {
    if (message.getId().equals(application.getConfiguration().getId())) {
      application.postInitialize();
    }
  }

  private void _handle() {
    Application.LOGGER.info("Found new master connection - Handshake!");
    natsService.send(Subject.PROCESSING, new HandshakeMessage(
        new ProcessingWorker(application.getConfiguration().getId(), application.getConfiguration().getServiceType())
    ));
  }
}
