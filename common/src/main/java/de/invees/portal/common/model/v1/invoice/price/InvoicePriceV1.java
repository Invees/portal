package de.invees.portal.common.model.v1.invoice.price;

import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvoicePriceV1 implements Model {

  private double raw;
  private double amount;
  private double taxes;

}
