package org.restonfire.exceptions;

/**
 * Created by jfischer on 2016-05-08.
 */
public class FirebaseInvalidStateException extends FirebaseRuntimeException {

  public FirebaseInvalidStateException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
