package org.restonfire;

/**
 * Created by jfischer on 2016-04-07.
 */
public interface FirebaseRestReferenceFactory {

  FirebaseRestReference getReference(String path);

  // TODO: Add FirebaseSecurityRulesReference getSecurityRules();
  // TODO: Add FirebaseEventStream getEventStream(String path);
}
