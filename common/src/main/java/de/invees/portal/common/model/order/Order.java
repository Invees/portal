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
  public static String BELONGS_TO = "belongsTo";
  public static String INVOICE = "invoice";
  public static String ORDER_TIME = "orderTime";
  public static String REQUEST = "request";
  public static String STATUS = "status";
  public static String REPLACED_WITH = "placedWith";

  @SerializedName("_id")
  private UUID id;
  private UUID belongsTo;
  private long invoice;
  private long orderTime;
  private OrderRequest request;
  private OrderStatus status;
  private UUID placedWith; // Order may be replaced by another order e.g more IPv4 Address, more ram or any upgrades

  public static String[] projection() {
    return new String[]{
        ID,
        BELONGS_TO,
        INVOICE,
        ORDER_TIME,
        REQUEST,
        STATUS,
        REPLACED_WITH
    };
  }
}
