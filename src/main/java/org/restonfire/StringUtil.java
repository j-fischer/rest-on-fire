package org.restonfire;

/**
 * Utility class for String operations.
 */
final class StringUtil {

  private StringUtil() {
    // do nothing
  }

  public static boolean notNullOrEmpty(String aString) {
    return aString != null && aString.length() > 0;
  }
}
