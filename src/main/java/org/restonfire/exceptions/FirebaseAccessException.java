package org.restonfire.exceptions;

import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Response;

import java.io.IOException;

/**
 * Exception thrown if the operation on a Firebase reference was not permitted
 * under the current security rules.
 */
public final class FirebaseAccessException extends FirebaseRuntimeException {

  public static final String ERROR_MESSAGE = "The access to the reference '%s' was not permitted. Status code: %s";

  public FirebaseAccessException(Response response)  throws IOException {
    super(ErrorCode.AccessViolation, String.format(ERROR_MESSAGE, response.getUri(), response.getStatusCode()));
  }

  public FirebaseAccessException(HttpResponseStatus responseStatus) {
    super(ErrorCode.AccessViolation, String.format(ERROR_MESSAGE, responseStatus.getUri(), responseStatus.getStatusCode()));
  }

  public FirebaseAccessException(String referenceUrl) {
    super(ErrorCode.AccessViolation, String.format("The access to the reference '%s' has been revoked.", referenceUrl));
  }
}
