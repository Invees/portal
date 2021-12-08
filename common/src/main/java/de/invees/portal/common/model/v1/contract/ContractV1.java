package de.invees.portal.common.model.v1.contract;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.order.OrderV1;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ContractV1 implements Model {

  public static String ID = "_id";
  public static String BELONGS_TO = "belongsTo";
  public static String TYPE = "type";
  public static String CREATED_AT = "createdAt";
  public static String ORDER = "order";
  public static String STATUS = "status";
  public static String REPLACED_WITH = "replacedWith";
  public static String IN_CANCELLATION = "inCancellation";

  @SerializedName("_id")
  private long id;
  private UUID belongsTo;
  private ContractTypeV1 type;
  private long createdAt;
  private OrderV1 order;
  private ContractStatusV1 status;
  private long replacedWith; // contract may be replaced by another contract e.g more IPv4 Address, more ram or any upgrades
  private boolean inCancellation;

  public static String[] projection() {
    return new String[]{
        ID,
        BELONGS_TO,
        TYPE,
        CREATED_AT,
        ORDER,
        STATUS,
        REPLACED_WITH,
        IN_CANCELLATION
    };
  }
}
