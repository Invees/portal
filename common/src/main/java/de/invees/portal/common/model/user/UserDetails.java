package de.invees.portal.common.model.user;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserDetails implements Model {

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
        User.ID,
        User.NAME,
        User.FIRST_NAME,
        User.LAST_NAME,
        User.EMAIL,
        User.PHONE,
        User.POST_CODE,
        User.CITY,
        User.ADDRESS,
        User.COMPANY_NAME,
        User.COUNTRY
    };
  }

}
