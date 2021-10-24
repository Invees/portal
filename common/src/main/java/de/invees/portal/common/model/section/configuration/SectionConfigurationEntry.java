package de.invees.portal.common.model.section.configuration;

import de.invees.portal.common.model.Display;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SectionConfigurationEntry {

  private String key;
  private Display displayName;
  private List<SectionConfigurationEntryOption> options;

}
