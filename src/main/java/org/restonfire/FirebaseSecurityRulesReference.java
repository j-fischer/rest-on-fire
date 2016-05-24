package org.restonfire;

import org.jdeferred.Promise;
import org.restonfire.exceptions.FirebaseRuntimeException;
import org.restonfire.responses.FirebaseSecurityRules;

/**
 * {@link FirebaseSecurityRulesReference} allows for updating of Firebase's security rules. It can be used
 * to get or set the security rules using the REST API.<br>
 * <br>
 * <b>Note: The security rules requires the access token to be a Firebase secret!</b>
 */
public interface FirebaseSecurityRulesReference {

  /**
   * Retrieves the rules currently configured for the {@link FirebaseRestNamespace}.
   *
   * @return The FirebaseSecurityRules
   */
  Promise<FirebaseSecurityRules, FirebaseRuntimeException, Void> get();

  /**
   * Replaces the existing security rules with the ones provided when invoking this method.
   * All previous rules will be replaced in this method.
   *
   * @param newRules The new set of rules that should be applied from now on.
   */
  Promise<FirebaseSecurityRules, FirebaseRuntimeException, Void> set(FirebaseSecurityRules newRules);
}
