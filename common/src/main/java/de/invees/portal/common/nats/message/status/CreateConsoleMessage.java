package de.invees.portal.common.nats.message.status;

import de.invees.portal.common.nats.message.processing.Message;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;
@Data
@AllArgsConstructor
public class CreateConsoleMessage implements Message {

  private UUID requestId;
  private UUID serviceId;

}
