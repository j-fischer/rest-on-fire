package org.restonfire.exceptions;

/**
 * Base exception of all Firebase related exceptions thrown by this API.
 * This will allow to set up a single catch block for all exception instead of
 * having to handle every exception separately.
 *
 * This exception is abstract and cannot be instantiated.
 * There should always be a more specific exception that
 * should be used when throwing an error.
 */
public abstract class FirebaseRuntimeException extends RuntimeException {

  private final ErrorCode errorCode;

  public FirebaseRuntimeException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public FirebaseRuntimeException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  /**
   * Enum describing the different errors that can occur within this library.
   */
  public enum ErrorCode {
    AccessViolation,
    AuthenticationExpired,
    ResponseDeserializationFailure,
    UnsupportedStatusCode,
    EventStreamListenerAlreadyActive,
    EventStreamListenerNotActive,
    QueryParamAlreadySet,
    EventStreamRequestFailed
  }
}
