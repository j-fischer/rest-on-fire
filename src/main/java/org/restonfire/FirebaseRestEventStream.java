package org.restonfire;

import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.restonfire.exceptions.FirebaseRuntimeException;
import org.restonfire.responses.StreamingEvent;

/**
 * A {@link FirebaseRestEventStream} represents a specific location within a Firebase namespace and allows
 * for listening to change events like data being set or updated.<br/>
 * <br/>
 * This interface is a mixture of Firebase's Java and Javascript API, using similar syntax as the Java (Android)
 * API but returning a {@link Promise} to allow for a more functional implementation of asynchronous requests.<br/>
 *
 * @see <a href="https://github.com/jdeferred/jdeferred">JDeferred Library</a>
 * @see #startListening() for more detailed information about this streaming events.
 */
public interface FirebaseRestEventStream {

  /**
   * Returns the fully qualified URL for this FirebaseRestEventStream instance.
   *
   * @return The absolute URL of the current reference as a String.
   */
  String getReferenceUrl();

  /**
   * Starts listening for the events on the current document location of this namespace. The first event will occur
   * immediately and contains the current value at the location. Any consecutive events depend on actual modifications
   * applied to the current location or its children.<br/>
   * <br/>
   * Individual events will be forwarded to the {@link Promise#progress(ProgressCallback)} function passing in an {@link StreamingEvent}
   * object that contains the type, a relative path and the value of the changed location in Firebase.
   * <br/>
   * The promise will be resolved once the <code>stopListening()</code> function is invoked.<br/>
   * <br/>
   * If the stream was forced to close by a change of security rules or expiration of the access token, the promise will
   * be rejected.
   *
   * @throws org.restonfire.exceptions.FirebaseInvalidStateException The listener for the events has already been started.
   * @throws org.restonfire.exceptions.FirebaseAccessException The client does not have permission to read from this location.
   * @throws org.restonfire.exceptions.FirebaseAuthenticationExpiredException The session for the given access token has expired.
   *
   * @return A {@link Promise} for the active request.
   */
  Promise<Void, FirebaseRuntimeException, StreamingEvent> startListening();

  /**
   * Closes the event stream, which will resolve the promise created for this location.
   *
   * @throws org.restonfire.exceptions.FirebaseInvalidStateException There is no event listener currently running.
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
