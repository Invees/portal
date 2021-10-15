package de.invees.portal.common.model.section.configuration;

import lombok.Data;

import java.util.List;

@Data
public class SectionConfigurationEntry {

  private final String key;
  private final String displayName;
  private final List<SectionConfigurationEntryOption> options;

}
