package org.restonfire.utils;

/**
 * Utility class for path operations.
 *
 * Created by jfischer on 2016-04-14.
 */
public final class PathUtil {

  private static final String FORWARD_SLASH = "/";
  private static final String PATH_CANNOT_BE_NULL = "path cannot be null";

  private PathUtil() {
    // do nothing
  }

  public static String getParent(String path) {
    if (path == null) {
      throw new IllegalArgumentException(PATH_CANNOT_BE_NULL);
    }

    final String normalizedPath = normalizePath(path);

    if (normalizedPath.length() == 0) {
      // root location
      return null;
    }

    final int index = normalizedPath.lastIndexOf('/');
    return index > 0
      ? path.substring(0, index)
      : "";
  }

  public static String getChild(String path, String child) {
    if (path == null) {
      throw new IllegalArgumentException(PATH_CANNOT_BE_NULL);
    }

    final String normalizedPath = normalizePath(path);

    return normalizedPath.length() > 0
      ? normalizedPath + FORWARD_SLASH + normalizePath(child)
      : normalizePath(child);
  }

  private static String normalizePath(String path) {
    return path.endsWith(FORWARD_SLASH)
      ? path.substring(0, path.length() - 1)
      : path;
  }
}
