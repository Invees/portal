package de.invees.portal.common.model.v1.service.status;

import de.invees.portal.common.model.v1.Model;
import de.invees.portal.common.model.v1.service.status.network.Network;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceStatus implements Model {

  private UUID service;
  private Map<String, Object> configuration;
  private ServiceStatusType status;
  private List<Network> network;
  private long uptime;

}
