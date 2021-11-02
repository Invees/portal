package de.invees.portal.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputUtils {

  public static Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\."
      + "[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");

  public static int asInteger(double data) {
    return (int) data;
  }

  public static boolean isInteger(double data) {
    if ((int) data == data) {
      return true;
    }
    return false;
  }

  public static int integerByString(String data, int defaultValue) {
    try {
      return Integer.valueOf(data);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public static boolean isInvalidEmailAddress(String email) {
    if (isEmpty(email)) {
      return true;
    }
    Matcher m = PATTERN.matcher(email);
    return !m.matches();
  }

  public static boolean isEmpty(String field) {
    if (field == null || field.isBlank() || field.isEmpty() || field.equalsIgnoreCase("")
        || field.equalsIgnoreCase(" ") || field.equalsIgnoreCase("undefined")) {
      return true;
    }
    return false;
  }

}
