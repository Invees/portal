package de.invees.portal.common.model.product.price;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OneOffPrice {

  private  double amount;
  private  int contractTerm;

}
