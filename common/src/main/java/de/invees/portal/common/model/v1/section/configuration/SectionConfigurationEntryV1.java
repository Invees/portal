package de.invees.portal.common.model.v1.section.configuration;

import de.invees.portal.common.model.v1.DisplayV1;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SectionConfigurationEntryV1 implements Model {

  private String key;
  private DisplayV1 displayName;
  private List<SectionConfigurationEntryOptionV1> optionList;

}
