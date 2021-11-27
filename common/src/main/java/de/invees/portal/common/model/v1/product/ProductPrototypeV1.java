package de.invees.portal.common.model.v1.product;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.v1.DisplayV1;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductPrototypeV1 implements Model {

  @SerializedName("_id")
  private String id;
  private String name;
  private DisplayV1 displayName;

  public static String[] projection() {
    return new String[]{
        ProductV1.ID,
        ProductV1.DisplayV1_NAME,
        ProductV1.SECTION
    };
  }

}
