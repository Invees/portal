package de.invees.portal.common.model.v1.service.console;

import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ServiceConsoleV1 implements Model {

  private ServiceConsoleTypeV1 type;
  private Map<String, Object> configuration;

}
