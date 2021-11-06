package de.invees.portal.common.model.service;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Service implements Model {

  public static String ID = "_id";
  public static String USER_ID = "userId";
  public static String PARENT_ORDER_ID = "parentOrderId";
  public static String WORKER_ID = "workerId";
  public static String SERVICE_TYPE = "type";

  @SerializedName("_id")
  private UUID id;
  private UUID userId;
  private UUID parentOrderId; // the order which belongs to this service, can be changed on upgrades etc.
  private UUID workerId;
  private ServiceType type;

  public static String[] projection() {
    return new String[]{
        ID, USER_ID, PARENT_ORDER_ID, WORKER_ID, SERVICE_TYPE
    };
  }
}
