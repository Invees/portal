package de.invees.portal.common.model.invoice;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.price.Price;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class Invoice implements Model {

  @SerializedName("_id")
  private final UUID id;
  private final String productId;
  private final UUID userId;
  private final UUID orderId;
  private final double price;
  private final long start;
  private final long end;
  private final Price monthly;
  private final Price oneOff;
  private final List<InvoiceConfigurationEntry> configuration;
  private final InvoiceStatus status;

}
