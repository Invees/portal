package de.invees.portal.common.datasource.response;

import de.invees.portal.common.model.v1.Model;
import lombok.Data;

import java.util.List;

@Data
public class PagedResponse<Y extends Model> {

  private final long count;
  private final List<Y> items;

}
