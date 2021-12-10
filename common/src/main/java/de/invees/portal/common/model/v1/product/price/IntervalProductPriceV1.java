package de.invees.portal.common.model.v1.product.price;

import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IntervalProductPriceV1 implements Model {

  private double amount;
  private int paymentInterval;

}
