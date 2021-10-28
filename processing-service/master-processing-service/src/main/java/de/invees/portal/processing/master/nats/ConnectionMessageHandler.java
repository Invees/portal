package de.invees.portal.processing.master.nats;

import de.invees.portal.common.nats.MessageHandler;
import de.invees.portal.common.nats.message.HandshakeMessage;
import de.invees.portal.common.nats.message.Message;
import de.invees.portal.processing.master.Application;
import de.invees.portal.processing.master.worker.WorkerRegistryService;
import lombok.Data;

@Data
public class ConnectionMessageHandler implements MessageHandler {

  private final WorkerRegistryService workerRegistry;

  @Override
  public void handle(Message message) {
    if (message instanceof HandshakeMessage) {
      _handle((HandshakeMessage) message);
    }
  }

  private void _handle(HandshakeMessage message) {
    Application.LOGGER.info("Worker with id '" + message.getWorker().getId() + "' joined the processing cluster.");
    workerRegistry.register(message.getWorker());
  }
}
