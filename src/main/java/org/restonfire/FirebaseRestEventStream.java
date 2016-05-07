package org.restonfire;

import org.jdeferred.Promise;
import org.restonfire.exceptions.FirebaseRuntimeException;
import org.restonfire.responses.EventStreamResponse;

/**
 * Created by jfischer on 2016-05-05.
 */
public interface FirebaseRestEventStream {

  /**
   * Starts listening for the events on the current document location of this namespace. The first event will occur
   * immediately and contains the current value at the location. Any consecutive events depend on actual modifications
   * applied to the current location or its children.<br>
   * <br>
   * The promise will be resolved once the <code>stopListening()</code> function is invoked.<br>
   * <br>
   * If the stream was forced to close by a change of security rules or expiration of the access token, the promise will
   * be rejected.
   *
   * @throws org.restonfire.exceptions.FirebaseAccessException The client does not have permission to read from this location.
   * @throws org.restonfire.exceptions.FirebaseAuthenticationExpiredException The session for the given access token has expired.
   *
   * @return A {@link Promise} for the active request.
   */
  Promise<Void, FirebaseRuntimeException, EventStreamResponse> startListening();

  /**
   * Closes the event stream, which will resolve the promise created for this location.
   */
  void stopListening();

  /**
   * Returns the streaming reference for the root of this Firebase namespace.
   *
   * @return The {@link FirebaseRestEventStream} representing the root of this Firebase namespace.
   */
  FirebaseRestEventStream getRoot();

  /**
   * Returns the streaming reference for the parent location of this {@link FirebaseRestEventStream}.
   *
   * @return The {@link FirebaseRestEventStream} representing the parent location of this {@link FirebaseRestEventStream}.
   */
  FirebaseRestEventStream getParent();

  /**
   * Returns the streaming reference for the given child location of this {@link FirebaseRestEventStream}.
   *
   * @param path The child's name or path to the child relative to this reference.
   * @return The {@link FirebaseRestEventStream} representing the child location.
   */
  FirebaseRestEventStream child(String path);

}
