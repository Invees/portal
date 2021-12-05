package de.invees.portal.common.model.v1.order;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class PrototypeOrderV1 implements Model {

  @SerializedName("_id")
  private long id;
  private UUID belongsTo;
  private OrderTypeV1 type;
  private long orderTime;
  private OrderStatusV1 status;

  public static String[] projection() {
    return new String[]{
        OrderV1.ID,
        OrderV1.BELONGS_TO,
        OrderV1.TYPE,
        OrderV1.ORDER_TIME,
        OrderV1.STATUS
    };
  }

}
