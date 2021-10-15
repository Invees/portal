package de.invees.portal.common.model.contract;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.Data;

import java.util.UUID;

@Data
public class ContractItem implements Model {

  @SerializedName("_id")
  private final UUID id;
  private final UUID userId;
  private final UUID orderId;

}
