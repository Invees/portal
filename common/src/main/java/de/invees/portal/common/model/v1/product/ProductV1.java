package de.invees.portal.common.model.v1.product;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.v1.DisplayV1;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.product.field.ProductFieldValueV1;
import de.invees.portal.common.model.v1.product.price.ProductPriceV1;
import de.invees.portal.common.model.v1.service.ServiceTypeV1;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ProductV1 implements Model {

  public static String ID = "_id";
  public static String DisplayV1_NAME = "displayName";
  public static String DESCRIPTION = "description";
  public static String SECTION = "section";
  public static String TYPE = "type";
  public static String FIELD_LIST = "fieldList";
  public static String PRICE = "price";
  public static String ACTIVE = "active";

  @SerializedName("_id")
  private String id;
  private String section;
  private DisplayV1 displayName;
  private String description;
  private Map<String, ProductFieldValueV1> fieldList;
  private ServiceTypeV1 type;
  private ProductPriceV1 price;
  private boolean active;

  public static String[] projection() {
    return new String[]{
        ID,
        DisplayV1_NAME,
        DESCRIPTION,
        SECTION,
        FIELD_LIST,
        TYPE,
        PRICE,
        ACTIVE
    };
  }

}
