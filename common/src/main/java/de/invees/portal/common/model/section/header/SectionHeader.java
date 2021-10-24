package de.invees.portal.common.model.section.header;

import de.invees.portal.common.model.Display;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.section.field.SectionField;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SectionHeader implements Model {

  private Display displayName;
  private List<SectionField> fields;

}
