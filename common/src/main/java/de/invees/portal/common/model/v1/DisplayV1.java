package de.invees.portal.common.model.v1;

import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DisplayV1 implements Model {

  private String de;
  private String en;

}
