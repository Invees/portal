package de.invees.portal.common.model.v1.order;

import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ContractUpgradeV1 implements Model {

  private final String key;
  private final Object value;

}
