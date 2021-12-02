package de.invees.portal.common.model.v1.service.software;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.service.ServiceTypeV1;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DisplaySoftwareV1 implements Model {

  @SerializedName("_id")
  private UUID id;
  private String name;
  private UUID belongsTo;
  private ServiceTypeV1 serviceType;
  private ServiceSoftwareTypeV1 type;

  public static String[] projection() {
    return new String[]{
        ServiceSoftwareV1.ID,
        ServiceSoftwareV1.NAME,
        ServiceSoftwareV1.BELONGS_TO,
        ServiceSoftwareV1.SERVICE_TYPE,
        ServiceSoftwareV1.TYPE
    };
  }
}
