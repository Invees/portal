package de.invees.portal.common.datasource.response;

import de.invees.portal.common.model.Model;
import lombok.Data;

import java.util.List;

@Data
public class PagedResponse<Y extends Model> {

  private final long count;
  private final List<Y> items;

}
