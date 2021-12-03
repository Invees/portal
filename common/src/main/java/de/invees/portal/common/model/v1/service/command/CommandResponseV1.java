package de.invees.portal.common.model.v1.service.command;

import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommandResponseV1 implements Model {

  private boolean success;
  private String message;

}
