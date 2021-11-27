package de.invees.portal.common.model.v1.section;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.v1.Display;
import de.invees.portal.common.model.v1.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SectionPrototype implements Model {

  @SerializedName("_id")
  private String id;
  private Display displayName;

  public static String[] projection() {
    return new String[]{
        Section.ID,
        Section.DISPLAY_NAME
    };
  }

}
