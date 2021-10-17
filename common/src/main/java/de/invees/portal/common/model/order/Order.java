package de.invees.portal.common.model.order;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.order.request.OrderRequest;
import lombok.Data;

import java.util.UUID;

@Data
public class Order implements Model {

  public static final String ID = "_id";
  public static final String USER_ID = "userId";
  public static final String ORDER_TIME = "orderTime";
  public static final String REQUEST = "request";
  public static final String STATUS = "status";

  @SerializedName("_id")
  private final UUID id;
  private final UUID userId;
  private final long orderTime;
  private final OrderRequest request;
  private final OrderStatus status;

  public static String[] projection() {
    return new String[]{
        ID,
        USER_ID,
        ORDER_TIME,
        REQUEST,
        STATUS
    };
  }
}
