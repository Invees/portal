package de.invees.portal.common.model.v1.section.header;

import de.invees.portal.common.model.v1.Display;
import de.invees.portal.common.model.v1.Model;
import de.invees.portal.common.model.v1.section.field.SectionField;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SectionHeader implements Model {

  private Display displayName;
  private List<SectionField> fieldList;

}
