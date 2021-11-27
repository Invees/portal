package de.invees.portal.common.model.v1.invoice;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.v1.Model;
import de.invees.portal.common.model.v1.price.Price;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Invoice implements Model {

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
  private Price price;
  private long date;
  private List<InvoicePosition> positionList;
  private InvoiceStatus status;

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
