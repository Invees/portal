package de.invees.portal.common.model.v1.product.field;

import de.invees.portal.common.model.v1.DisplayV1;
import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductFieldValueV1 implements Model {

  private DisplayV1 displayValue;
  private Object value;

}
