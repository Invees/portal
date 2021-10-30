package de.invees.portal.common.nats.message.processing;

import lombok.Data;

import java.util.UUID;

@Data
public class KeepAliveMessage implements Message {

  private final UUID id;

}
