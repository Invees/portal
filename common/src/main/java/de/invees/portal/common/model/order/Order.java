package de.invees.portal.common.model.order;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.order.request.OrderRequest;
import de.invees.portal.common.model.product.Product;
import de.invees.portal.common.model.invoice.Invoice;
import de.invees.portal.common.model.section.Section;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class Order implements Model {

  @SerializedName("_id")
  private final UUID id;
  private final UUID userId;
  private final long orderTime;
  private final OrderRequest request;
  private final OrderStatus status;

}
