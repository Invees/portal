package de.invees.portal.common.nats.message.processing;

import de.invees.portal.common.model.v1.contract.ContractV1;
import de.invees.portal.common.nats.message.Message;
import lombok.Data;

import java.util.UUID;

@Data
public class ExecuteOrderMessage implements Message {

  private final UUID workerId;
  private final ContractV1 contract;

}
