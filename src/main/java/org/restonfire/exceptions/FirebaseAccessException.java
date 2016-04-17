package org.restonfire.exceptions;

import com.ning.http.client.Response;

/**
 * Created by jfischer on 2016-04-16.
 */
public class FirebaseAccessException extends FirebaseRuntimeException {
  public FirebaseAccessException(Response response) {
  }
}
