package de.invees.portal.common.model.v1.section.tag;

import de.invees.portal.common.model.v1.DisplayV1;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SectionTagV1 implements Model {

  private DisplayV1 displayTitle;
  private DisplayV1 displayValue;

}
