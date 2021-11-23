package de.invees.portal.common.model.service.console;

import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.service.status.ServiceStatusType;
import lombok.Data;

import java.util.Map;

@Data
public class ServiceConsole implements Model {

  private ServiceStatusType type;
  private Map<String, Object> configuration;

}
