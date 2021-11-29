package de.invees.portal.common.model.v1.service.software;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceSoftwareV1 implements Model {

  @SerializedName("_id")
  private UUID id;
  private String name;
  private UUID belongsTo;
  
}
