package de.invees.portal.common.model.v1.service.software;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.ApiInterfaceIgnore;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.service.ServiceTypeV1;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@ApiInterfaceIgnore
public class ServiceSoftwareV1 implements Model {

  public static String ID = "_id";
  public static String NAME = "name";
  public static String INTERNAL_NAME = "internalName";
  public static String SERVICE_TYPE = "serviceType";
  public static String TYPE = "type";
  public static String BELONGS_TO = "belongsTo";

  @SerializedName("_id")
  private UUID id;
  private String name;
  private String internalName;
  private ServiceTypeV1 serviceType;
  private ServiceSoftwareTypeV1 type;
  private UUID belongsTo;

  public static String[] projection() {
    return new String[]{
        ID, NAME, INTERNAL_NAME, SERVICE_TYPE, TYPE, BELONGS_TO
    };
  }
}