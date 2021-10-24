package de.invees.portal.common.model.user.permission;

import de.invees.portal.common.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Permission implements Model {

  public static String NAME = "name";
  public static String CONTEXT = "context";

  private String name;
  private List<String> contextList;

}
