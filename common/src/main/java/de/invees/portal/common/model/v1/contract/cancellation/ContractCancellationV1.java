package de.invees.portal.common.model.v1.contract.cancellation;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ContractCancellationV1 implements Model {

  public static String ID = "_id";
  public static String CONTRACT = "contract";
  public static String CREATED_AT = "createdAt";
  public static String EFFECTIVE_AT = "effectiveAt";
  public static String CANCEL = "cancel";

  @SerializedName("_id")
  private UUID id;
  private long contract;
  private long createdAt;
  private long effectiveAt;
  private boolean cancel;

  public static String[] projection() {
    return new String[]{
        ID,
        CONTRACT,
        CREATED_AT,
        EFFECTIVE_AT,
        CANCEL
    };
  }
}
