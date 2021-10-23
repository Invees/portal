package de.invees.portal.common.utils.template;

import java.io.File;
import java.nio.file.Files;

public class TemplateUtils {

  public static String loadTemplate(String name) {
    try {
      return Files.readString(new File("./template/" + name).toPath());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


}
