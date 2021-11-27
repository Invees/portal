package de.invees.portal.common.model.v1.section;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.v1.Display;
import de.invees.portal.common.model.v1.Model;
import de.invees.portal.common.model.v1.section.configuration.SectionConfigurationEntry;
import de.invees.portal.common.model.v1.section.header.SectionHeader;
import de.invees.portal.common.model.v1.section.tag.SectionTag;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Section implements Model {

  public static String ID = "_id";
  public static String DISPLAY_NAME = "displayName";
  public static String DESCRIPTION = "description";
  public static String STRUCTURE_LIST = "structureList";
  public static String CONFIGURATION_LIST = "configurationList";
  public static String TAG_LIST = "tagList";
  public static String ACTIVE = "active";

  @SerializedName("_id")
  private String id;
  private Display displayName;
  private Display description;
  private List<SectionHeader> structureList;
  private List<SectionConfigurationEntry> configurationList;
  private List<SectionTag> tagList;
  private boolean active;

  public static String[] projection() {
    return new String[]{
        ID, DISPLAY_NAME, DESCRIPTION, STRUCTURE_LIST, CONFIGURATION_LIST, TAG_LIST, ACTIVE
    };
  }

}
