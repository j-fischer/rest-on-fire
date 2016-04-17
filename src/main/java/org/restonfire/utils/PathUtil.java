package org.restonfire.utils;

/**
 * Created by jfischer on 2016-04-14.
 */
public final class PathUtil {
  private PathUtil() {
    // do nothing
  }

  public static String getParent(String path) {
    if (path.endsWith("/"))
      return path.substring(0, path.lastIndexOf('/', path.length() - 2));
    else
      return path.substring(0, path.lastIndexOf('/'));
  }

  public static String getChild(String path, String child) {
    if (path.endsWith("/"))
      return path + child;
    else
      return path + "/" + child;
  }
}
