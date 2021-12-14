package de.invees.portal.common.model.v1.service.software;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.service.ServiceTypeV1;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceSoftwareV1 implements Model {

  public static String ID = "_id";
  public static String NAME = "name";
  public static String SERVICE_TYPE = "serviceType";
  public static String TYPE = "type";
  public static String BELONGS_TO = "belongsTo";
  public static String CREATED_AT = "createdAt";

  @SerializedName("_id")
  private UUID id;
  private String name;
  private ServiceTypeV1 serviceType;
  private ServiceSoftwareTypeV1 type;
  private UUID belongsTo;
  private long createdAt;

  public static String[] projection() {
    return new String[]{
        ID, NAME, SERVICE_TYPE, TYPE, BELONGS_TO, CREATED_AT
    };
  }
}
