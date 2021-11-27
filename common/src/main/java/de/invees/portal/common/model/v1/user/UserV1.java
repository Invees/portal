package de.invees.portal.common.model.v1.user;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.ApiInterfaceIgnore;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@ApiInterfaceIgnore
public class UserV1 implements Model {

  public static String ID = "_id";
  public static String NAME = "name";
  public static String FIRST_NAME = "firstName";
  public static String LAST_NAME = "lastName";
  public static String EMAIL = "email";
  public static String PHONE = "phone";
  public static String COMPANY_NAME = "companyName";
  public static String PERMISSION_LIST = "permissionList";
  public static String POST_CODE = "postCode";
  public static String CITY = "city";
  public static String COUNTRY = "country";
  public static String ADDRESS = "address";
  public static String VERIFIED = "verified";

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
  private boolean verified;

  public static String[] projection() {
    return new String[]{
        ID,
        NAME,
        FIRST_NAME,
        LAST_NAME,
        EMAIL,
        PHONE,
        PERMISSION_LIST,
        POST_CODE,
        CITY,
        ADDRESS,
        COMPANY_NAME,
        COUNTRY,
        VERIFIED
    };
  }

}
