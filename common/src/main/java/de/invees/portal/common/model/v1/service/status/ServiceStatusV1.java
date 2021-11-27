package de.invees.portal.common.model.v1.service.status;

import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.service.status.network.ServiceNetworkV1;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceStatusV1 implements Model {

  private UUID service;
  private Map<String, Object> configuration;
  private ServiceStatusTypeV1 status;
  private List<ServiceNetworkV1> network;
  private long uptime;

}
