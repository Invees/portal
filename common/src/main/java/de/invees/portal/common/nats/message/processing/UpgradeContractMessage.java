package de.invees.portal.common.nats.message.processing;

import de.invees.portal.common.model.v1.order.ContractUpgradeV1;
import de.invees.portal.common.nats.message.Message;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class UpgradeContractMessage implements Message {

  private final long contract;
  private final List<ContractUpgradeV1> upgrades;

}
