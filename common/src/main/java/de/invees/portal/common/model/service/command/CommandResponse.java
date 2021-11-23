package de.invees.portal.common.model.service.command;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.Data;

import java.util.UUID;

@Data
public class CommandResponse implements Model {

  @SerializedName("_id")
  private final UUID id;
  private final boolean processed;

}
