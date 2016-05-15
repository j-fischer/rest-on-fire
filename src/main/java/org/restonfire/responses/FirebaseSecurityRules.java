package org.restonfire.responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * POJO representing the Firebase rules model consisting of a single property named "rules",
 * which contains a map of nested key-value pairs. Keys are always strings, while objects can
 * either be boolean, an expression string, or a map of key-value pairs for the node's children.<br>
 * <br>
 * This class does also contain constants for common key names such as ".read".
 *
 * @see <a href="https://www.firebase.com/docs/security/guide/index.html">Firebase Security Rules</a>
 */
public class FirebaseSecurityRules {

  private static final Type RULES_TYPE = new TypeToken<Map<String, Object>>() { }.getType();

  /**
   * The key to be used for setting write access.
   */
  public static final String WRITE_KEY = ".write";

  /**
   * The key to be used for setting read access.
   */
  public static final String READ_KEY = ".read";

  /**
   * The key to be used for validating new inputs.
   */
  public static final String VALIDATE_KEY = ".validate";

  /**
   * The key to be used when setting an index.
   */
  public static final String INDEX_KEY = ".indexOn";

  private final Map<String, Object> rules;

  public FirebaseSecurityRules() {
    this(null);
  }

  private transient final Gson gson = new GsonBuilder()
    .disableHtmlEscaping()
    .create();

  public FirebaseSecurityRules(Map<String, Object> rules) {
    this.rules = rules;
  }

  public Map<String, Object> getRules() {
    //TODO: Review if using gson is the best method for the deep copy
    return gson.fromJson(gson.toJson(rules), RULES_TYPE);
  }
}
