package de.invees.portal.common.model.v1.user;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.v1.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserNameDetails implements Model {

  @SerializedName("_id")
  private UUID id;
  private String name;
  private String firstName;
  private String lastName;
  private String companyName;

  public static String[] projection() {
    return new String[]{
        User.ID,
        User.NAME,
        User.FIRST_NAME,
        User.LAST_NAME,
        User.COMPANY_NAME
    };
  }
}
