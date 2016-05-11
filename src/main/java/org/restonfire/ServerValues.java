package org.restonfire;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains all Firebase server values such as a timestamp.
 */
public final class ServerValues {

  /**
   * A server timestamp value.
   *
   * @see <a href="https://www.firebase.com/docs/rest/api/#section-server-values">Firebase REST Server Values Documentation</a>
   */
  public static final Map<String, String> TIMESTAMP;

  static {
    final HashMap<String, String> timestamp = new HashMap<>(1);
    timestamp.put(".sv", "timestamp");
    TIMESTAMP = Collections.unmodifiableMap(timestamp);
  }

  private ServerValues() {
    // do not use
  }
}
