package de.invees.portal.common.model.v1.product.price;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OneOffPrice {

  private double amount;
  private int contractTerm;

}
