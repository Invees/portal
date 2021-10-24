package de.invees.portal.common.model.service;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Service implements Model {

  @SerializedName("_id")
  private UUID id;
  private UUID userId;
  private UUID orderId;

}
