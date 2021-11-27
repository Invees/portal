package de.invees.portal.common.model.v1.product.field;

import de.invees.portal.common.model.v1.Display;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductFieldValue {

  private Display displayValue;
  private Object value;

}
