package de.invees.portal.common.model.v1.user;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserNameDetailsV1 implements Model {

  @SerializedName("_id")
  private UUID id;
  private String name;
  private String firstName;
  private String lastName;
  private String companyName;

  public static String[] projection() {
    return new String[]{
        UserV1.ID,
        UserV1.NAME,
        UserV1.FIRST_NAME,
        UserV1.LAST_NAME,
        UserV1.COMPANY_NAME
    };
  }
}
