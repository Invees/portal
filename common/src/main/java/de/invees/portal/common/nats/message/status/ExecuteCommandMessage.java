package de.invees.portal.common.nats.message.status;

import de.invees.portal.common.model.service.command.Command;
import de.invees.portal.common.nats.message.processing.Message;
import lombok.Data;

@Data
public class ExecuteCommandMessage implements Message {

  private final Command command;

}
