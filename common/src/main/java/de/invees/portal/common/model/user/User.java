package de.invees.portal.common.model.user;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.user.permission.Permission;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class User implements Model {

  public static final String ID = "_id";
  public static final String NAME = "name";
  public static final String FIRST_NAME = "firstName";
  public static final String LAST_NAME = "lastName";
  public static final String EMAIL = "email";
  public static final String PHONE = "phone";
  public static final String COMPANY_NAME = "companyName";
  public static final String PERMISSIONS = "permissions";
  public static final String POST_CODE = "postCode";
  public static final String CITY = "city";
  public static final String ADDRESS = "address";

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
  private final List<Permission> permissions;

  public static String[] projection() {
    return new String[]{
        ID,
        NAME,
        FIRST_NAME,
        LAST_NAME,
        EMAIL,
        PHONE,
        PERMISSIONS,
        POST_CODE,
        CITY,
        ADDRESS,
        COMPANY_NAME
    };
  }

  public boolean isPermitted(String name, String context) {
    Permission permission = getPermission(name);
    if (permission == null) {
      return false;
    }
    return permission.getContext().contains(context);
  }

  public Permission getPermission(String name) {
    if (permissions == null) {
      return null;
    }
    for (Permission permission : permissions) {
      if (permission.getName().equalsIgnoreCase(name)) {
        return permission;
      }
    }
    return null;
  }

}
