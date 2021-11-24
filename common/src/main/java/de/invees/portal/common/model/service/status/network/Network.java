package de.invees.portal.common.model.service.status.network;

import lombok.Data;

@Data
public class Network {

  private final NetworkType type;
  private final String value;
  private final String gateway;

}
