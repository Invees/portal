package de.invees.portal.common.model.v1.section.configuration;

import de.invees.portal.common.model.v1.Display;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SectionConfigurationEntry {

  private String key;
  private Display displayName;
  private List<SectionConfigurationEntryOption> optionList;

}
