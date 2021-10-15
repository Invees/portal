package de.invees.portal.common.model.user.permission;

import de.invees.portal.common.model.Model;
import lombok.Data;

import java.util.List;

@Data
public class Permission implements Model {

  public static final String NAME = "name";
  public static final String CONTEXT = "context";

  private final String name;
  private final List<String> context;

}
