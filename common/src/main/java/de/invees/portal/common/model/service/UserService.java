package de.invees.portal.common.model.service;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.order.request.OrderRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserService implements Model {

  @SerializedName("_id")
  private UUID id;
  private UUID userId;
  private UUID initialOrderId;
  private OrderRequest orderRequest;

}
