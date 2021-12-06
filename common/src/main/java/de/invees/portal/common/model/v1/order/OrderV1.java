package de.invees.portal.common.model.v1.order;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.order.request.OrderRequestV1;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class OrderV1 implements Model {

  public static String ID = "_id";
  public static String BELONGS_TO = "belongsTo";
  public static String TYPE = "type";
  public static String ORDER_TIME = "orderTime";
  public static String REQUEST = "request";
  public static String STATUS = "status";
  public static String REPLACED_WITH = "replacedWith";

  @SerializedName("_id")
  private long id;
  private UUID belongsTo;
  private OrderTypeV1 type;
  private long orderTime;
  private OrderRequestV1 request;
  private OrderStatusV1 status;
  private long replacedWith; // Order may be replaced by another order e.g more IPv4 Address, more ram or any upgrades

  public static String[] projection() {
    return new String[]{
        ID,
        BELONGS_TO,
        TYPE,
        ORDER_TIME,
        REQUEST,
        STATUS,
        REPLACED_WITH
    };
  }
}
