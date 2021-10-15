package de.invees.portal.common.model.product.price;

import lombok.Data;

import java.util.List;

@Data
public class ProductPrice {

  private final double amount;
  private final List<OneOffPrice> oneOff;

}
