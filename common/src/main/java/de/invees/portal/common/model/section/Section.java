package de.invees.portal.common.model.section;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Display;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.section.configuration.SectionConfigurationEntry;
import de.invees.portal.common.model.section.header.SectionHeader;
import de.invees.portal.common.model.section.tag.SectionTag;
import lombok.Data;

import java.util.List;

@Data
public class Section implements Model {

  public static final String ID = "_id";
  public static final String DISPLAY_NAME = "displayName";
  public static final String DESCRIPTION = "description";
  public static final String STRUCTURE = "structure";
  public static final String CONFIGURATION = "configuration";
  public static final String TAGS = "tags";
  public static final String ACTIVE = "active";

  @SerializedName("_id")
  private final String id;
  private final Display displayName;
  private final Display description;
  private final List<SectionHeader> structure;
  private final List<SectionConfigurationEntry> configuration;
  private final List<SectionTag> tags;
  private final boolean active;

  public static String[] projection() {
    return new String[]{
        ID, DISPLAY_NAME, DESCRIPTION, STRUCTURE, CONFIGURATION, TAGS, ACTIVE
    };
  }

}
