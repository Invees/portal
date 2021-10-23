package de.invees.portal.common.model.invoice;

import de.invees.portal.common.model.Display;
import de.invees.portal.common.model.order.request.OrderRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class InvoicePosition {

  private  Display displayName;
  private  Display displayValue;
  private  Object value;
  private  String key;
  private  double price;
  private  double priceWithAddons;
  private  OrderRequest orderRequest;
  private  List<InvoicePosition> positions;

}
