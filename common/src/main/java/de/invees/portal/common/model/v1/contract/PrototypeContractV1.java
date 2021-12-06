package de.invees.portal.common.model.v1.contract;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class PrototypeContractV1 implements Model {

  @SerializedName("_id")
  private long id;
  private UUID belongsTo;
  private ContractTypeV1 type;
  private long orderAt;
  private ContractStatusV1 status;

  public static String[] projection() {
    return new String[]{
        ContractV1.ID,
        ContractV1.BELONGS_TO,
        ContractV1.TYPE,
        ContractV1.CREATED_AT,
        ContractV1.STATUS
    };
  }

}
