package de.invees.portal.common.model.order;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.order.request.OrderRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Order implements Model {

  public static String ID = "_id";
  public static String USER_ID = "userId";
  public static String INVOICE_ID = "invoiceId";
  public static String ORDER_TIME = "orderTime";
  public static String REQUEST = "request";
  public static String STATUS = "status";

  @SerializedName("_id")
  private UUID id;
  private UUID userId;
  private long invoiceId;
  private long orderTime;
  private OrderRequest request;
  private OrderStatus status;

  public static String[] projection() {
    return new String[]{
        ID,
        USER_ID,
        INVOICE_ID,
        ORDER_TIME,
        REQUEST,
        STATUS
    };
  }
}
