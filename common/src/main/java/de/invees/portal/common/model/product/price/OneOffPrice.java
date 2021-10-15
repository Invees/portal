package de.invees.portal.common.model.product.price;

import lombok.Data;

@Data
public class OneOffPrice {

  private final double amount;
  private final int contractTerm;

}
