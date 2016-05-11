package org.restonfire.exceptions;

import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Response;

import java.io.IOException;

/**
 * A request to Firebase's REST API failed with an unexpected status code.
 */
public final class FirebaseRestException extends FirebaseRuntimeException {

  private static final String ERROR_MESSAGE = "The REST request to '%s' failed with the following status code: %s";

  public FirebaseRestException(ErrorCode errorCode, Response response) throws IOException {
    super(errorCode, String.format(ERROR_MESSAGE, response.getUri(), response.getStatusCode()));
  }

  public FirebaseRestException(ErrorCode errorCode, HttpResponseStatus responseStatus) {
    super(errorCode, String.format(ERROR_MESSAGE, responseStatus.getUri(), responseStatus.getStatusCode()));
  }

  public FirebaseRestException(ErrorCode errorCode, String message, Throwable cause) {
    super(errorCode, message, cause);
  }
}
