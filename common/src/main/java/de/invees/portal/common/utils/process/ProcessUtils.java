package de.invees.portal.common.utils.process;

import java.io.IOException;

public class ProcessUtils {

  public static Process exec(String command) throws IOException {
    return Runtime.getRuntime().exec(command);
  }

  public static Process exec(String command, String[] arguments) throws IOException {
    String fullCommand = command;
    for(String argument : arguments) {
      fullCommand += " ";
      fullCommand += argument;
    }
    return exec(fullCommand);
  }

}
