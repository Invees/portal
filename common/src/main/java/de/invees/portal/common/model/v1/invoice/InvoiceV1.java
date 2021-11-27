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
  public static String SERVICE_LIST = "serviceList";
  public static String PRICE = "price";
  public static String DATE = "date";
  public static String POSITION_LIST = "positionList";
  public static String STATUS = "status";

  @SerializedName("_id")
  private long id;
  private UUID belongsTo;
  private List<UUID> serviceList;
  private InvoicePriceV1 price;
  private long date;
  private List<InvoicePositionV1> positionList;
  private InvoiceStatusV1 status;

  public static String[] projection() {
    return new String[]{
        ID,
        BELONGS_TO,
        SERVICE_LIST,
        PRICE,
        DATE,
        POSITION_LIST,
        STATUS
    };
  }
}
