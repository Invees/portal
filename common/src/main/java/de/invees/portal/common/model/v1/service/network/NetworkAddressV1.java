package de.invees.portal.common.model.v1.service.network;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class NetworkAddressV1 implements Model {

  public static String ID = "_id";
  public static String TYPE = "type";
  public static String ADDRESS = "address";
  public static String NETMASK = "netmask";
  public static String GATEWAY = "gateway";
  public static String SERVICE = "service";

  @SerializedName("_id")
  private UUID id;
  private NetworkAddressTypeV1 type;
  private String address;
  private String netmask;
  private String gateway;
  private UUID service;

  public static String[] projection() {
    return new String[]{
        ID, TYPE, ADDRESS, NETMASK, GATEWAY, SERVICE
    };
  }
}
