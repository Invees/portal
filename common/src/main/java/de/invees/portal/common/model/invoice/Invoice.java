package de.invees.portal.common.model.invoice;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.price.Price;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class Invoice implements Model {

  public static final String ID = "_id";
  public static final String PRODUCT_ID = "productId";
  public static final String USER_ID = "userId";
  public static final String ORDER_ID = "orderId";
  public static final String PRICE = "price";
  public static final String START = "start";
  public static final String END = "end";
  public static final String MONTHLY = "monthly";
  public static final String ONE_OFF = "oneOff";
  public static final String CONFIGURATION = "configuration";
  public static final String STATUS = "status";

  @SerializedName("_id")
  private final int id;
  private final String productId;
  private final UUID userId;
  private final UUID orderId;
  private final double price;
  private final long start;
  private final long end;
  private final Price monthly;
  private final Price oneOff;
  private final List<InvoiceConfigurationEntry> configuration;
  private final InvoiceStatus status;

  public static String[] projection() {
    return new String[]{
        ID,
        PRODUCT_ID,
        USER_ID,
        ORDER_ID,
        PRICE,
        START,
        END,
        MONTHLY,
        ONE_OFF,
        CONFIGURATION,
        STATUS
    };
  }
}
