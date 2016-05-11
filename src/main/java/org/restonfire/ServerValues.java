package org.restonfire;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains all Firebase server values such as a timestamp.
 * @see <a href="https://www.firebase.com/docs/rest/api/#section-server-values">Firebase REST Server Values Documentation</a>
 */
public final class ServerValues {

  public static final Map<String, String> TIMESTAMP;
  static {
    TIMESTAMP = new HashMap<>(1);
    TIMESTAMP.put(".sv", "timestamp");
  }
}
