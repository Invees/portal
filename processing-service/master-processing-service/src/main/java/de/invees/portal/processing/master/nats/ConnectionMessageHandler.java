package de.invees.portal.processing.master.nats;

import de.invees.portal.common.nats.MessageHandler;
import de.invees.portal.common.nats.NatsService;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.HandshakeMessage;
import de.invees.portal.common.nats.message.processing.KeepAliveMessage;
import de.invees.portal.common.nats.message.processing.Message;
import de.invees.portal.common.nats.message.processing.WelcomeMessage;
import de.invees.portal.processing.master.Application;
import de.invees.portal.processing.master.worker.WorkerRegistryService;
import lombok.Data;

@Data
public class ConnectionMessageHandler implements MessageHandler {

  private final WorkerRegistryService workerRegistry;
  private final NatsService natsService;

  @Override
  public void handle(Message message) {
    if (message instanceof HandshakeMessage) {
      _handle((HandshakeMessage) message);
    } else if (message instanceof KeepAliveMessage) {
      _handle((KeepAliveMessage) message);
    }
  }

  private void _handle(HandshakeMessage message) {
    Application.LOGGER.info("Worker with id '" + message.getWorker().getId() + "' joined the processing cluster.");
    workerRegistry.register(message.getWorker());
    natsService.send(Subject.PROCESSING, new WelcomeMessage(message.getWorker().getId()));
  }

  private void _handle(KeepAliveMessage message) {
    workerRegistry.keepAlive(message.getId());
  }
}
