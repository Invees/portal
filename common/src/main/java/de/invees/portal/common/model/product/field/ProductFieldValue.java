package de.invees.portal.common.model.product.field;

import de.invees.portal.common.model.Display;
import lombok.Data;

@Data
public class ProductFieldValue {

  private final Display displayValue;
  private final Object value;

}
