package de.invees.portal.common.model.v1.order.request;

import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class OrderRequestV1 implements Model {

  private String product;
  private Map<String, Object> configuration;
  private int contractTerm;

}
