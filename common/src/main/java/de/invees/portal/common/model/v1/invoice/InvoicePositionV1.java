package de.invees.portal.common.model.v1.invoice;

import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.DisplayV1;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class InvoicePositionV1 implements Model {

  private DisplayV1 displayName;
  private DisplayV1 displayValue;
  private Object value;
  private String key;
  private double price;
  private double priceWithAddons;
  private de.invees.portal.common.model.v1.order.request.OrderRequestV1 orderRequest;
  private List<InvoicePositionV1> positionList;

}
