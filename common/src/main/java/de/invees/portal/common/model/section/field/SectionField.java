package de.invees.portal.common.model.section.field;

import de.invees.portal.common.model.Display;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SectionField implements Model {

  private  String key;
  private  Display displayName;
  private  SectionFieldType type;

}
