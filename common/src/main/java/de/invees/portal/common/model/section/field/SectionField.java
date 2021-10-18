package de.invees.portal.common.model.section.field;

import de.invees.portal.common.model.Display;
import de.invees.portal.common.model.Model;
import lombok.Data;

@Data
public class SectionField implements Model {

  private final String key;
  private final Display displayName;
  private final SectionFieldType type;

}
