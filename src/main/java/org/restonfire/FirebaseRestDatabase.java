package org.restonfire;

/**
 * A {@link FirebaseRestDatabase} is a base representation of a Firebase database. It
 * manages all resources for the {@link FirebaseRestReference} implementations as well as
 * handles authentication for that database.
 */
public interface FirebaseRestDatabase {

  /**
   * Returns a {@link FirebaseRestReference} object representing the provided location of the database.
   * With this reference object, the consumer can read or modify the data at this location, or travers
   * further down or up the tree.
   *
   * @param path The location within database to create a reference for.
   * @return The {@link FirebaseRestReference} for the given path.
   */
  FirebaseRestReference getReference(String path);

  /**
   *
   * @param path The location within database to create a event stream for.
   * @return The
   */
  FirebaseRestEventStream getEventStream(String path);
  // TODO: Add FirebaseSecurityRulesReference getSecurityRules();
}
