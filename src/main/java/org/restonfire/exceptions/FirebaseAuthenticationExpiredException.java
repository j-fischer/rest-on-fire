package org.restonfire.exceptions;

/**
 * Created by jfischer on 2016-05-06.
 */
public class FirebaseAuthenticationExpiredException extends FirebaseRuntimeException {

  public FirebaseAuthenticationExpiredException(String referenceUrl) {
    super("The access token for this connection has expired. Location: " + referenceUrl);
  }
}
