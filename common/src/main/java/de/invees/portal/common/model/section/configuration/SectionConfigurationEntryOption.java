package de.invees.portal.common.model.section.configuration;

import de.invees.portal.common.model.Display;
import lombok.Data;

@Data
public class SectionConfigurationEntryOption {

  private final Object value;
  private final Display displayValue;
  private final double price;

}
