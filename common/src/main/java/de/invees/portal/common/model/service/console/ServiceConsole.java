package de.invees.portal.common.model.service.console;

import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.service.status.ServiceStatusType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ServiceConsole implements Model {

  private ServiceConsoleType type;
  private Map<String, Object> configuration;

}
