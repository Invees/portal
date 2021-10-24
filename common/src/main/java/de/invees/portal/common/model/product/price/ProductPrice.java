package de.invees.portal.common.model.product.price;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductPrice {

  private  double amount;
  private  List<OneOffPrice> oneOff;

}
