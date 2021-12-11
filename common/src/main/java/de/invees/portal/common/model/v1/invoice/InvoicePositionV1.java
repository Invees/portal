package de.invees.portal.common.model.v1.invoice;

import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.DisplayV1;
import de.invees.portal.common.model.v1.order.OrderV1;
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
  private OrderV1 order;
  private List<InvoicePositionV1> positionList;
  private int interval;
  private double pricePerMonth;

}
