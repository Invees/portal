package de.invees.portal.common.model.invoice;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.UUID;

@Data
public class InvoiceFile {

  @SerializedName("_id")
  private final UUID id;
  private final UUID invoiceId;
  private final byte[] file;

}
