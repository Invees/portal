package de.invees.portal.common.model.section;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Display;
import de.invees.portal.common.model.Model;
import lombok.Data;

@Data
public class SectionPrototype implements Model {

  @SerializedName("_id")
  private final String id;
  private final Display displayName;

  public static String[] projection() {
    return new String[]{
        Section.ID,
        Section.DISPLAY_NAME
    };
  }

}
