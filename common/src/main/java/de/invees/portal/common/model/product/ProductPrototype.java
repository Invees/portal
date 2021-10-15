package de.invees.portal.common.model.product;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.Data;

@Data
public class ProductPrototype implements Model {

  @SerializedName("_id")
  private final String id;
  private final String name;
  private final String displayName;

  public static String[] projection() {
    return new String[]{
        Product.ID,
        Product.DISPLAY_NAME,
        Product.SECTION_ID
    };
  }

}
