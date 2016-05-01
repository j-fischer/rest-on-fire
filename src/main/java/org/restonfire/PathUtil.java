package org.restonfire;

/**
 * Utility class for path operations.
 *
 * Created by jfischer on 2016-04-14.
 */
final class PathUtil {

  public static final String FORWARD_SLASH = "/";

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

  public static String concatenatePath(String path, String child) {
    if (path == null) {
      throw new IllegalArgumentException(PATH_CANNOT_BE_NULL);
    }

    final String normalizedPath = normalizePath(path);
    final String normalizedChild = normalizePath(child);

    return normalizedPath.length() > 0
      ? normalizedPath + FORWARD_SLASH + normalizedChild
      : normalizedChild;
  }

  public static String normalizePath(String path) {
    return path.endsWith(FORWARD_SLASH)
      ? path.substring(0, path.length() - 1)
      : path;
  }
}
