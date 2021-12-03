package de.invees.portal.common.nats.message.status;

import de.invees.portal.common.model.v1.service.command.CommandResponseV1;
import de.invees.portal.common.nats.message.Message;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CommandResponseMessage implements Message {

  private UUID requestId;
  private CommandResponseV1 response;

}
