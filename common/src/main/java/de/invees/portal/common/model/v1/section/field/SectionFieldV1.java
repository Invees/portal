package de.invees.portal.common.model.v1.section.field;

import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.DisplayV1;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SectionFieldV1 implements Model {

  private String key;
  private DisplayV1 displayName;
  private SectionFieldTypeV1 type;

}
