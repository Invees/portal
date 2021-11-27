package de.invees.portal.common.nats.message.processing;

import de.invees.portal.common.model.v1.order.OrderV1;
import lombok.Data;

import java.util.UUID;

@Data
public class ExecuteOrderMessage implements Message {

  private final UUID workerId;
  private final OrderV1 order;

}
