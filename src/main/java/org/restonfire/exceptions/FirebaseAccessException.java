package org.restonfire.exceptions;

import com.ning.http.client.Response;

import java.io.IOException;

/**
 * Exception thrown if the operation on a Firebase reference was not permitted
 * under the current security rules.
 */
public final class FirebaseAccessException extends FirebaseRuntimeException {

  public FirebaseAccessException(Response response)  throws IOException {
    super(String.format("The access to the reference '%s' was not permitted. Status code: %s", response.getUri(), response.getStatusCode()));
  }
}
