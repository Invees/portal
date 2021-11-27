package de.invees.portal.common.model.v1.service.status.network;

import de.invees.portal.common.model.Model;
import lombok.Data;

@Data
public class ServiceNetworkV1 implements Model {

  private final ServiceNetworkTypeV1 type;
  private final String value;
  private final String gateway;

}
