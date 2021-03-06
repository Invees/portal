package de.invees.portal.common.model.v1.user;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.ApiInterfaceIgnore;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@ApiInterfaceIgnore
public class UserAuthenticationV1 implements Model {

  public static String ID = "_id";
  public static String USER = "user";
  public static String TYPE = "type";
  public static String DATA = "data";

  @SerializedName("_id")
  private UUID id;
  private UUID user;
  private UserAuthenticationTypeV1 type;
  private Map<String, Object> data;

  public static String[] projection() {
    return new String[]{
        ID,
        USER,
        TYPE,
        DATA
    };
  }
}
