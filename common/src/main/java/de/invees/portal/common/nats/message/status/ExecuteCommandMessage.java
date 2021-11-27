package de.invees.portal.common.nats.message.status;

import de.invees.portal.common.model.v1.service.command.CommandV1;
import de.invees.portal.common.nats.message.processing.Message;
import lombok.Data;

@Data
public class ExecuteCommandMessage implements Message {

  private final CommandV1 command;

}
