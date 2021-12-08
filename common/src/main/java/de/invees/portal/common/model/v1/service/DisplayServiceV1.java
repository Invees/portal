package de.invees.portal.common.model.v1.service;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DisplayServiceV1 implements Model {

  @SerializedName("_id")
  private UUID id;
  private String name;
  private UUID belongsTo;
  private long contract; // the contract which belongs to this service, can be changed on upgrades etc.
  private UUID worker;
  private ServiceTypeV1 type;

  public static String[] projection() {
    return new String[]{
        ServiceV1.ID, ServiceV1.NAME, ServiceV1.BELONGS_TO, ServiceV1.CONTRACT, ServiceV1.WORKER, ServiceV1.SERVICE_TYPE
    };
  }
}
