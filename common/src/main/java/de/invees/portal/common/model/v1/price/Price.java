package de.invees.portal.common.model.v1.price;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Price {

  private double raw;
  private double amount;
  private double taxes;

}
