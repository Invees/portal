package de.invees.portal.common.model.section;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Display;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.section.configuration.SectionConfigurationEntry;
import de.invees.portal.common.model.section.field.SectionField;
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
  public static String FIELD_LIST = "fieldList";
  public static String CONFIGURATION_LIST = "configurationList";
  public static String TAG_LIST = "tagList";
  public static String ACTIVE = "active";

  @SerializedName("_id")
  private String id;
  private Display displayName;
  private Display description;
  private List<SectionField> fieldList;
  private List<SectionConfigurationEntry> configurationList;
  private List<SectionTag> tagList;
  private boolean active;

  public static String[] projection() {
    return new String[]{
        ID, DISPLAY_NAME, DESCRIPTION, FIELD_LIST, CONFIGURATION_LIST, TAG_LIST, ACTIVE
    };
  }

}
