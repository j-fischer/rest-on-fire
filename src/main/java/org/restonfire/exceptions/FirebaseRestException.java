package org.restonfire.exceptions;

import com.ning.http.client.Response;

/**
 * Created by jfischer on 2016-04-16.
 */
public class FirebaseRestException extends FirebaseRuntimeException {
  public FirebaseRestException(Response response) {
  }

  public FirebaseRestException(String message, Exception e) {
  }
}
