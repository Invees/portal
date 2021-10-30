package de.invees.portal.common.nats.message.processing;

import de.invees.portal.common.model.worker.ProcessingWorker;
import lombok.Data;

@Data
public class HandshakeMessage implements Message {

  private final ProcessingWorker worker;

}
