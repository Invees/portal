package de.invees.portal.common.model.invoice;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.price.Price;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Invoice implements Model {

  public static String ID = "_id";
  public static String USER_ID = "userId";
  public static String PRICE = "price";
  public static String DATE = "date";
  public static String POSITIONS = "positions";
  public static String STATUS = "status";

  @SerializedName("_id")
  private long id;
  private UUID userId;
  private Price price;
  private long date;
  private List<InvoicePosition> positions;
  private InvoiceStatus status;

  public static String[] projection() {
    return new String[]{
        ID,
        USER_ID,
        PRICE,
        DATE,
        POSITIONS,
        STATUS
    };
  }
}
