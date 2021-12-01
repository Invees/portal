package de.invees.portal.common.utils.process;

import java.io.File;
import java.io.IOException;

public class ProcessUtils {

  public static void execAndWait(String command) throws IOException {
    execAndWait(command, null);
  }

  public static void execAndWait(String command, File directory) throws IOException {
    Process process = exec(command, directory);
    while (process.isAlive()) {
    }
  }

  public static Process exec(String command, File directory) throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
    processBuilder.redirectErrorStream(true);
    if (directory != null) {
      processBuilder.directory(directory);
    }
    return processBuilder.start();
  }

  public static Process exec(String command, String[] arguments, File directory) throws IOException {
    String fullCommand = command;
    for (String argument : arguments) {
      fullCommand += " ";
      fullCommand += argument;
    }
    return exec(fullCommand, directory);
  }

}
