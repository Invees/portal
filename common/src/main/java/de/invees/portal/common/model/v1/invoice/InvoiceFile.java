package de.invees.portal.common.model.v1.invoice;

import com.google.gson.annotations.SerializedName;
import de.invees.portal.common.model.v1.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvoiceFile implements Model {

  public static String ID = "_id";
  public static String DATA = "data";

  @SerializedName("_id")
  private long id;
  private byte[] data;

  public static String[] projection() {
    return new String[]{
        ID,
        DATA,
    };
  }
}
