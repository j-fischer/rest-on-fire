package org.restonfire.exceptions;

import com.ning.http.client.Response;

import java.io.IOException;

/**
 * A request to Firebase's REST API failed with an unexpected status code.
 *
 * Created by jfischer on 2016-04-16.
 */
public class FirebaseRestException extends FirebaseRuntimeException {

  public FirebaseRestException(Response response) throws IOException {
    super(String.format("The REST request to '%s' failed with the following status code: %s", response.getUri(), response.getStatusCode()));
  }

  public FirebaseRestException(String message, Exception cause) {
    super(message, cause);
  }

}
