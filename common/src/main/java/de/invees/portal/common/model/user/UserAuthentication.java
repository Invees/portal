package de.invees.portal.common.model.user;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class UserAuthentication implements Model {

  public static final String ID = "_id";
  public static final String USER_ID = "userId";
  public static final String TYPE = "type";
  public static final String DATA = "data";

  @SerializedName("_id")
  private final UUID id;
  private final UUID userId;
  private final UserAuthenticationType type;
  private final Map<String, Object> data;

  public static String[] projection() {
    return new String[]{
        ID,
        USER_ID,
        TYPE,
        DATA
    };
  }
}
