package de.invees.portal.common.model.section;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Display;
import de.invees.portal.common.model.Model;
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
