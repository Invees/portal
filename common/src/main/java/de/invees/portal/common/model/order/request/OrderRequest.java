package de.invees.portal.common.model.order.request;

import de.invees.portal.common.model.Model;
import lombok.Data;

import java.util.Map;

@Data
public class OrderRequest implements Model {

  private final String productId;
  private final Map<String, Object> configuration;
  private final int contractTerm;

}
