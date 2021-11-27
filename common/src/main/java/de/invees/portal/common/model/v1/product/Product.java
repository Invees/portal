package de.invees.portal.common.model.v1.product;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.v1.Display;
import de.invees.portal.common.model.v1.Model;
import de.invees.portal.common.model.v1.product.field.ProductFieldValue;
import de.invees.portal.common.model.v1.product.price.ProductPrice;
import de.invees.portal.common.model.v1.service.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Product implements Model {

  public static String ID = "_id";
  public static String DISPLAY_NAME = "displayName";
  public static String DESCRIPTION = "description";
  public static String SECTION = "section";
  public static String TYPE = "type";
  public static String FIELD_LIST = "fieldList";
  public static String PRICE = "price";
  public static String ACTIVE = "active";

  @SerializedName("_id")
  private String id;
  private String section;
  private Display displayName;
  private String description;
  private Map<String, ProductFieldValue> fieldList;
  private ServiceType type;
  private ProductPrice price;
  private boolean active;

  public static String[] projection() {
    return new String[]{
        ID,
        DISPLAY_NAME,
        DESCRIPTION,
        SECTION,
        FIELD_LIST,
        TYPE,
        PRICE,
        ACTIVE
    };
  }

}
