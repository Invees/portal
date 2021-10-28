package de.invees.portal.common.model.product.field;

import de.invees.portal.common.model.Display;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductFieldValue {

  private Display displayValue;
  private Object value;

}
