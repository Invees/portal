package de.invees.portal.common.model.user;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.Data;

import java.util.UUID;

@Data
public class DisplayUser implements Model {

  @SerializedName("_id")
  private final UUID id;
  private final String name;
  private final String firstName;
  private final String lastName;
  private final String email;
  private final String phone;
  private final String companyName;
  private final String postCode;
  private final String city;
  private final String address;

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
        User.COMPANY_NAME
    };
  }

}
