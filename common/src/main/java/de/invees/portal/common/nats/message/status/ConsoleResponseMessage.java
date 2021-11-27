package de.invees.portal.common.nats.message.status;

import de.invees.portal.common.model.v1.service.console.ServiceConsoleV1;
import de.invees.portal.common.nats.message.processing.Message;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ConsoleResponseMessage implements Message {

  private UUID requestId;
  private ServiceConsoleV1 console;

}
