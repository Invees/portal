package de.invees.portal.common.model.gateway;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
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
