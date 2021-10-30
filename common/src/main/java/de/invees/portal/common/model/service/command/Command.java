package de.invees.portal.common.model.service.command;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class Command implements Model {

  @SerializedName("_id")
  private UUID id;
  private UUID userId;
  private UUID serviceId;
  private String action;
  private Map<String, Object> data;

}
