package de.invees.portal.common.model.v1.service.status;

import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceStatusV1 implements Model {

  private UUID service;
  private Map<String, Object> configuration;
  private ServiceStatusTypeV1 status;
  private long lastStart;

}
