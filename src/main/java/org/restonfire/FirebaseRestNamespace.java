package org.restonfire;

/**
 * A {@link FirebaseRestNamespace} is a base representation of a Firebase namespace. It
 * manages all resources for the {@link FirebaseRestReference} implementations as well as
 * handles authentication for that namespace.
 */
public interface FirebaseRestNamespace {

  /**
   * Returns a {@link FirebaseRestReference} object representing the provided location of the namespace.
   * With this reference object, the consumer can read or modify the data at this location, or travers
   * further down or up the tree.
   *
   * @param path The location within namespace to create a reference for.
   * @return The {@link FirebaseRestReference} for the given path.
   */
  FirebaseRestReference getReference(String path);

  /**
   * Returns a {@link FirebaseRestEventStream} object representing the provided location of the namespace.
   * With this reference object, the consumer can listen to changes for the data at this location, or travers
   * further down or up the tree.
   *
   * @param path The location within namespace to create a event stream for.
   * @return The {@link FirebaseRestEventStream} for the given path.
   */
  FirebaseRestEventStream getEventStream(String path);

  /**
   * Returns a {@link FirebaseSecurityRulesReference} object for this namespaces. The object can be used
   * to retrieve or modify the access rules, add validation or indexes.
   *
   * @return The {@link FirebaseSecurityRulesReference} for this namespace.
   */
  FirebaseSecurityRulesReference getSecurityRules();
}
