package de.invees.portal.common.model.section.configuration;

import de.invees.portal.common.model.Display;
import lombok.Data;

import java.util.List;

@Data
public class SectionConfigurationEntry {

  private final String key;
  private final Display displayName;
  private final List<SectionConfigurationEntryOption> options;

}
