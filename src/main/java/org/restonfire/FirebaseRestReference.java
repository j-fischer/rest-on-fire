package org.restonfire;


import org.jdeferred.Promise;
import org.restonfire.exceptions.FirebaseRuntimeException;

/**
 * A {@link FirebaseRestReference} represents a specific location within a Firebase database and allows
 * for operations to be executed on this location using Firebase's REST API.<br>
 * <br>
 * This interface is a mixture of Firebase's Java and Javascript API, using similar syntax as the Java (Android)
 * API but returning a {@link Promise} to allow for a more functional implementation of asynchronous requests.
 *
 * @see <a href="https://github.com/jdeferred/jdeferred">JDeferred Library</a>
 */
public interface FirebaseRestReference {

  /**
   * Returns the fully qualified URL for this FirebaseRestReference instance.
   *
   * @return The absolute URL of the current reference as a String.
   */
  String getReferenceUrl();

  /**
   * Retrieves the value for this reference URL from Firebase.<br>
   * <br>
   * The promise returned will be rejected with the following two exceptions:<br>
   * <ul>
   *   <li><b>org.restonfire.exceptions.FirebaseAccessException</b> - A {@link FirebaseRuntimeException} in the case that
   *        access to the data for this reference was denied.
   *   </li>
   *   <li><b>org.restonfire.exceptions.FirebaseRestException</b> - A {@link FirebaseRuntimeException} in the case that an
   *        unexpected status code was returned or the deserialization of the response into the type parameter fails.
   *   </li>
   * </ul>
   * @param clazz The {@link Class} type for the POJO to be created for the data returned by the request.
   * @param <T> The type of the result object.
   * @return A promise which will be resolved with the POJO generated from the response if the request was successful.
   */
  <T> Promise<T, FirebaseRuntimeException, Void> getValue(Class<T> clazz);

  /**
   * Sets the value in Firebase for this reference URL. This will overwrite all data that is currently stored
   * under that location. Providing a <code>null</code> value is the equivalent of removing the data at this location.<br>
   * <br>
   * The promise returned will be rejected with the following two exceptions:<br>
   * <ul>
   *   <li><b>org.restonfire.exceptions.FirebaseAccessException</b> - A {@link FirebaseRuntimeException} in the case that
   *        access to the data for this reference was denied.
   *   </li>
   *   <li><b>org.restonfire.exceptions.FirebaseRestException</b> - A {@link FirebaseRuntimeException} in the case that an
   *        unexpected status code was returned or the deserialization of the response into the type parameter fails.
   *   </li>
   * </ul>
   * @param value The value to be written to Firebase.
   * @param <T> The type of the parameter object.
   * @return A promise which will resolve with the object passed in as the value if the request was successful.
   */
  <T> Promise<T, FirebaseRuntimeException, Void> setValue(T value);

  /**
   * Updates the value in Firebase for this reference URL. Only the properties (child nodes) that
   * are included in this value object will be overwritten. Any other properties not included in the value
   * will remain the same.<br>
   * <br>
   * The promise returned will be rejected with the following two exceptions:<br>
   * <ul>
   *   <li><b>org.restonfire.exceptions.FirebaseAccessException</b> - A {@link FirebaseRuntimeException} in the case that
   *        access to the data for this reference was denied.
   *   </li>
   *   <li><b>org.restonfire.exceptions.FirebaseRestException</b> - A {@link FirebaseRuntimeException} in the case that an
   *        unexpected status code was returned or the deserialization of the response into the type parameter fails.
   *   </li>
   * </ul>
   * @param value The value to be written to Firebase.
   * @param <T> The type of the parameter object.
   * @return A promise which will resolve with the object passed in as the value if the request was successful.
   */
  <T> Promise updateValue(T value);

  /**
   * Removes the value for this reference URL from Firebase. This is the requivalent to setting the value
   * to <code>null</code>.<br>
   * <br>
   * The promise returned will be rejected with the following two exceptions:<br>
   * <ul>
   *   <li><b>org.restonfire.exceptions.FirebaseAccessException</b> - A {@link FirebaseRuntimeException} in the case that
   *        access to the data for this reference was denied.
   *   </li>
   *   <li><b>org.restonfire.exceptions.FirebaseRestException</b> - A {@link FirebaseRuntimeException} in the case that
   *        an unexpected status code was returned.
   *   </li>
   * </ul>
   * @return A promise which will be resolved with a <code>null</code> value if the request was successful.
   */
  Promise<Void, FirebaseRuntimeException, Void> removeValue();

  /**
   * Creates a new child property under the current location. Firebase will give the child node a unique
   * identifier returned by this .<br>
   * <br>
   * The promise returned will be rejected with the following two exceptions:<br>
   * <ul>
   *   <li><b>org.restonfire.exceptions.FirebaseAccessException</b> - A {@link FirebaseRuntimeException} in the case that
   *        access to the data for this reference was denied.
   *   </li>
   *   <li><b>org.restonfire.exceptions.FirebaseRestException</b> - A {@link FirebaseRuntimeException} in the case that
   *        an unexpected status code was returned.
   *   </li>
   * </ul>
   * @return The reference to the newly created child in Firebase.
   *
   * @see <a href="https://www.firebase.com/docs/android/api/#firebase_push">Firebase Android Documentation</a>
   */
  Promise<FirebaseRestReference, FirebaseRuntimeException, Void> push();

  /**
   * Returns the reference for the root of this Firebase database.
   *
   * @return The {@link FirebaseRestReference} representing the root of this Firebase database.
   */
  FirebaseRestReference getRoot();

  /**
   * Returns the reference for the parent location of this {@link FirebaseRestReference}.
   *
   * @return The {@link FirebaseRestReference} representing the parent location of this {@link FirebaseRestReference}.
   */
  FirebaseRestReference getParent();

  /**
   * Returns the reference for the given child location of this {@link FirebaseRestReference}.
   *
   * @param path The child's name or path to the child relative to this reference.
   * @return The {@link FirebaseRestReference} representing the child location.
   */
  FirebaseRestReference child(String path);

  /**
   * Returns a {@link FirebaseRestQuery} object, which can be used to apply a large number of filters when
   * retrieving values for this location. This is most useful for maps of objects like chat messages, for example.
   * A query can be used to sort and filter the result set based on Firebase's sorting rules.<br>
   * <br>
   * A {@link FirebaseRestQuery} object can be used to make multiple requests, either with the last supplied filters,
   * or with a new set of filters to be applied after calling the <code>query.clear()</code> function.<br>
   * <br>
   * <a href="https://www.firebase.com/docs/rest/guide/retrieving-data.html">Firebase's old documentation</a> is also a
   * great resource to understand the behaviour of the supported methods.
   *
   * @return The {@link FirebaseRestQuery} representing this location.
   * @see <a href="https://firebase.google.com/docs/database/rest/retrieve-data#section-rest-filtering">Firebase Filtering Data Documentation</a>
   */
  FirebaseRestQuery query();

  //TODO: Add support for priorities to getValue/setValue
}
