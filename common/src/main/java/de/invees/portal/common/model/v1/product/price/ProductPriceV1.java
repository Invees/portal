package de.invees.portal.common.model.v1.product.price;

import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductPriceV1 implements Model {

  private List<IntervalProductPriceV1> intervalList;
  private List<OneOffProductPriceV1> oneOffList;

}
