package de.invees.portal.common.model.product;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Display;
import de.invees.portal.common.model.product.price.ProductPrice;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.product.field.ProductFieldValue;
import lombok.Data;

import java.util.Map;

@Data
public class Product implements Model {

  public static final String ID = "_id";
  public static final String DISPLAY_NAME = "displayName";
  public static final String DESCRIPTION = "displayName";
  public static final String SECTION_ID = "sectionId";
  public static final String FIELDS = "fields";
  public static final String PRICE = "price";
  public static final String ACTIVE = "active";

  @SerializedName("_id")
  private final String id;
  private final String sectionId;
  private final Display displayName;
  private final String description;
  private final Map<String, ProductFieldValue> fields;
  private final ProductPrice price;
  private final boolean active;

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
