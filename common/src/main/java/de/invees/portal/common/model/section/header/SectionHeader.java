package de.invees.portal.common.model.section.header;

import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.section.field.SectionField;
import lombok.Data;

import java.util.List;

@Data
public class SectionHeader implements Model {

  private final String displayName;
  private final List<SectionField> fields;

}