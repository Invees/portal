package de.invees.portal.common.utils;

import java.io.File;

public class IOUtils {

  public static boolean delete(File dir) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        return delete(new File(dir, children[i]));
      }
    }
    return dir.delete();
  }

}
