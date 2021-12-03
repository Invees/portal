package de.invees.portal.common.nats.message.processing;

import de.invees.portal.common.model.v1.worker.ProcessingWorkerV1;
import de.invees.portal.common.nats.message.Message;
import lombok.Data;

@Data
public class HandshakeMessage implements Message {

  private final ProcessingWorkerV1 worker;

}
