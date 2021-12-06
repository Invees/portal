package de.invees.portal.common.model.v1.invoice;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.invoice.price.InvoicePriceV1;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class InvoiceV1 implements Model {

  public static String ID = "_id";
  public static String BELONGS_TO = "belongsTo";
  public static String CONTRACT_LIST = "contractList";
  public static String PRICE = "price";
  public static String CREATED_AT = "createdAt";
  public static String PAID_AT = "paidAt";
  public static String POSITION_LIST = "positionList";
  public static String STATUS = "status";

  @SerializedName("_id")
  private long id;
  private UUID belongsTo;
  private List<Long> contractList;
  private InvoicePriceV1 price;
  private long createdAt;
  private long paidAt;
  private List<InvoicePositionV1> positionList;
  private InvoiceStatusV1 status;

  public static String[] projection() {
    return new String[]{
        ID,
        BELONGS_TO,
        CONTRACT_LIST,
        PRICE,
        CREATED_AT,
        PAID_AT,
        POSITION_LIST,
        STATUS
    };
  }
}
