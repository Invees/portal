package de.invees.portal.common.model.section.configuration;

import de.invees.portal.common.model.Display;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SectionConfigurationEntryOption {

  private  Object value;
  private  Display displayValue;
  private  double price;

}
