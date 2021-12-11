package de.invees.portal.common.model.v1.section;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.DisplayV1;
import de.invees.portal.common.model.v1.section.configuration.SectionConfigurationEntryV1;
import de.invees.portal.common.model.v1.section.field.SectionFieldV1;
import de.invees.portal.common.model.v1.section.tag.SectionTagV1;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SectionV1 implements Model {

  public static String ID = "_id";
  public static String DisplayV1_NAME = "displayName";
  public static String DESCRIPTION = "description";
  public static String FIELD_LIST = "fieldList";
  public static String CONFIGURATION_LIST = "configurationList";
  public static String TAG_LIST = "tagList";
  public static String ACTIVE = "active";

  @SerializedName("_id")
  private String id;
  private DisplayV1 displayName;
  private DisplayV1 description;
  private List<SectionFieldV1> fieldList;
  private List<SectionConfigurationEntryV1> configurationList;
  private List<SectionTagV1> tagList;
  private boolean active;

  public static String[] projection() {
    return new String[]{
        ID, DisplayV1_NAME, DESCRIPTION, FIELD_LIST, CONFIGURATION_LIST, TAG_LIST, ACTIVE
    };
  }

}
