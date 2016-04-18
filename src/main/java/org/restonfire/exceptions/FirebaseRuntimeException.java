package org.restonfire.exceptions;

/**
 * Base exception of all Firebase related exceptions thrown by this API.
 * This will allow to set up a single catch block for all exception instead of
 * having to handle every exception separately.
 *
 * This exception is abstract and cannot be instantiated.
 * There should always be a more specific exception that
 * should be used when throwing an error.
 *
 * Created by jfischer on 2016-04-16.
 */
public abstract class FirebaseRuntimeException extends RuntimeException {

  public FirebaseRuntimeException(String message) {
    super(message);
  }

  public FirebaseRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
