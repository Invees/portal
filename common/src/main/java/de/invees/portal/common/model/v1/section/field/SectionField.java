package de.invees.portal.common.model.v1.section.field;

import de.invees.portal.common.model.v1.Display;
import de.invees.portal.common.model.v1.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SectionField implements Model {

  private String key;
  private Display displayName;
  private SectionFieldType type;

}
