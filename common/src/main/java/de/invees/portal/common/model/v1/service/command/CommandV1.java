package de.invees.portal.common.model.v1.service.command;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CommandV1 implements Model {

  @SerializedName("_id")
  private UUID id;
  private UUID executor;
  private UUID service;
  private String action;
  private Map<String, Object> data;

}
