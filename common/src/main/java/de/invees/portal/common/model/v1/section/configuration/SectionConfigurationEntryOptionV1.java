package de.invees.portal.common.model.v1.section.configuration;

import de.invees.portal.common.model.v1.DisplayV1;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SectionConfigurationEntryOptionV1 implements Model {

  private Object value;
  private DisplayV1 displayValue;
  private double price;

}
