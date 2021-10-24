package de.invees.portal.common.model.user;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.user.permission.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class User implements Model {

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
  private List<Permission> permissionList;

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
        COUNTRY
    };
  }

  public boolean isPermitted(String name, String context) {
    Permission permission = getPermission(name);
    if (permission == null) {
      return false;
    }
    return permission.getContextList().contains(context);
  }

  public Permission getPermission(String name) {
    if (permissionList == null) {
      return null;
    }
    for (Permission permission : permissionList) {
      if (permission.getName().equalsIgnoreCase(name)) {
        return permission;
      }
    }
    return null;
  }

}
