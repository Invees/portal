package de.invees.portal.common.model.product;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Display;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.product.field.ProductFieldValue;
import de.invees.portal.common.model.product.price.ProductPrice;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Product implements Model {

  public static String ID = "_id";
  public static String DISPLAY_NAME = "displayName";
  public static String DESCRIPTION = "displayName";
  public static String SECTION_ID = "sectionId";
  public static String FIELDS = "fields";
  public static String PRICE = "price";
  public static String ACTIVE = "active";

  @SerializedName("_id")
  private String id;
  private String sectionId;
  private Display displayName;
  private String description;
  private Map<String, ProductFieldValue> fields;
  private ProductPrice price;
  private boolean active;

  public static String[] projection() {
    return new String[]{
        ID,
        DISPLAY_NAME,
        DESCRIPTION,
        SECTION_ID,
        FIELDS,
        PRICE,
        ACTIVE
    };
  }

}
