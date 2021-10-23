package de.invees.portal.common.model.section;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Display;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.section.configuration.SectionConfigurationEntry;
import de.invees.portal.common.model.section.header.SectionHeader;
import de.invees.portal.common.model.section.tag.SectionTag;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Section implements Model {

  public static String ID = "_id";
  public static String DISPLAY_NAME = "displayName";
  public static String DESCRIPTION = "description";
  public static String STRUCTURE = "structure";
  public static String CONFIGURATION = "configuration";
  public static String TAGS = "tags";
  public static String ACTIVE = "active";

  @SerializedName("_id")
  private String id;
  private Display displayName;
  private Display description;
  private List<SectionHeader> structure;
  private List<SectionConfigurationEntry> configuration;
  private List<SectionTag> tags;
  private boolean active;

  public static String[] projection() {
    return new String[]{
        ID, DISPLAY_NAME, DESCRIPTION, STRUCTURE, CONFIGURATION, TAGS, ACTIVE
    };
  }

}
