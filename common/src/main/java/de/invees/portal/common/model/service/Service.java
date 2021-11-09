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
  public static String NAME = "name";
  public static String BELONGS_TO = "belongsTo";
  public static String PARENT_ORDER = "parentOrder";
  public static String WORKER = "worker";
  public static String SERVICE_TYPE = "type";

  @SerializedName("_id")
  private UUID id;
  private String name;
  private UUID belongsTo;
  private UUID parentOrder; // the order which belongs to this service, can be changed on upgrades etc.
  private UUID worker;
  private ServiceType type;

  public static String[] projection() {
    return new String[]{
        ID, NAME, BELONGS_TO, PARENT_ORDER, WORKER, SERVICE_TYPE
    };
  }
}
