package de.invees.portal.common.model.v1.section.configuration;

import de.invees.portal.common.model.v1.Display;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SectionConfigurationEntryOption {

  private Object value;
  private Display displayValue;
  private double price;

}
