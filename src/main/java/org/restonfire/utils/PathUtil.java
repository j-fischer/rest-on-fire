package org.restonfire.utils;

/**
 * Utility class for path operations.
 *
 * Created by jfischer on 2016-04-14.
 */
public final class PathUtil {
  private PathUtil() {
    // do nothing
  }

  public static String getParent(String path) {
    if (path == null) {
      throw new IllegalArgumentException("path cannot be null");
    }

    String normalizedPath = normalizePath(path);

    if (normalizedPath.length() == 0) {
      // root location
      return null;
    }

    int index = normalizedPath.lastIndexOf('/');
    return index > 0 ?
      path.substring(0, index) :
      "";
  }

  public static String getChild(String path, String child) {
    if (path == null) {
      throw new IllegalArgumentException("path cannot be null");
    }

    String normalizedPath = normalizePath(path);

    return normalizedPath.length() > 0 ?
      normalizedPath + "/" + normalizePath(child) :
      normalizePath(child);
  }

  private static String normalizePath(String path) {
    return path.endsWith("/") ?
        path.substring(0, path.length() - 1) :
        path;
  }
}
