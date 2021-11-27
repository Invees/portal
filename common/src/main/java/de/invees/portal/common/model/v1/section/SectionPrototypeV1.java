package de.invees.portal.common.model.v1.section;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.DisplayV1;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SectionPrototypeV1 implements Model {

  @SerializedName("_id")
  private String id;
  private DisplayV1 displayName;

  public static String[] projection() {
    return new String[]{
        SectionV1.ID,
        SectionV1.DisplayV1_NAME
    };
  }

}
