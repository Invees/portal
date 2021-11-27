package de.invees.portal.common.model.v1.user;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserDetailsV1 implements Model {

  @SerializedName("_id")
  private UUID id;
  private String name;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private String companyName;
  private String postCode;
  private String city;
  private String address;
  private String country;

  public static String[] projection() {
    return new String[]{
        UserV1.ID,
        UserV1.NAME,
        UserV1.FIRST_NAME,
        UserV1.LAST_NAME,
        UserV1.EMAIL,
        UserV1.PHONE,
        UserV1.POST_CODE,
        UserV1.CITY,
        UserV1.ADDRESS,
        UserV1.COMPANY_NAME,
        UserV1.COUNTRY
    };
  }

}
