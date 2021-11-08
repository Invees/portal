package de.invees.portal.common.model.product;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Display;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductPrototype implements Model {

  @SerializedName("_id")
  private String id;
  private String name;
  private Display displayName;

  public static String[] projection() {
    return new String[]{
        Product.ID,
        Product.DISPLAY_NAME,
        Product.SECTION
    };
  }

}
