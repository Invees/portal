package de.invees.portal.processing.master.nats;

import de.invees.portal.common.nats.MessageHandler;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.HandshakeMessage;
import de.invees.portal.common.nats.message.processing.KeepAliveMessage;
import de.invees.portal.common.nats.message.processing.Message;
import de.invees.portal.common.nats.message.processing.WelcomeMessage;
import de.invees.portal.processing.master.Application;
import de.invees.portal.processing.master.worker.WorkerRegistryProvider;
import lombok.Data;

@Data
public class ProcessingMessageHandler implements MessageHandler {

  private final WorkerRegistryProvider workerRegistry;
  private final NatsProvider natsProvider;

  @Override
  public void handle(Message message) {
    if (message instanceof HandshakeMessage) {
      execHandle((HandshakeMessage) message);
    } else if (message instanceof KeepAliveMessage) {
      execHandle((KeepAliveMessage) message);
    }
  }

  private void execHandle(HandshakeMessage message) {
    Application.LOGGER.info("Worker with id '" + message.getWorker().getId() + "' joined the processing cluster.");
    workerRegistry.register(message.getWorker());
    natsProvider.send(Subject.PROCESSING, new WelcomeMessage(message.getWorker().getId()));
  }

  private void execHandle(KeepAliveMessage message) {
    workerRegistry.keepAlive(message.getId());
  }
}
