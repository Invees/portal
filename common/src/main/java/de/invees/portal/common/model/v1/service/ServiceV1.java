package de.invees.portal.common.model.v1.service;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceV1 implements Model {

  public static String ID = "_id";
  public static String NAME = "name";
  public static String BELONGS_TO = "belongsTo";
  public static String CONTRACT = "contract";
  public static String WORKER = "worker";
  public static String SERVICE_TYPE = "type";

  @SerializedName("_id")
  private UUID id;
  private String name;
  private UUID belongsTo;
  private long contract; // the contract which belongs to this service, can be changed on upgrades etc.
  private UUID worker;
  private ServiceTypeV1 type;

  public static String[] projection() {
    return new String[]{
        ID, NAME, BELONGS_TO, CONTRACT, WORKER, SERVICE_TYPE
    };
  }
}
