package de.invees.portal.common.nats.message.processing;

import lombok.Data;

import java.util.UUID;

@Data
public class ServiceCreatedMessage implements Message {

  private final UUID orderId;
  private final UUID serviceId;
  private final UUID workerId;

}
