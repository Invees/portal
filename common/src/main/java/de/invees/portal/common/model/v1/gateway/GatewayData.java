package de.invees.portal.common.model.v1.gateway;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.v1.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class GatewayData implements Model {

  @SerializedName("_id")
  private UUID id;
  private GatewayDataType type;
  private Object data;

}
