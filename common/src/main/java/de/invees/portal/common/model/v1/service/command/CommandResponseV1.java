package de.invees.portal.common.model.v1.service.command;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.Data;

import java.util.UUID;

@Data
public class CommandResponseV1 implements Model {

  @SerializedName("_id")
  private final UUID id;
  private final boolean processed;

}
