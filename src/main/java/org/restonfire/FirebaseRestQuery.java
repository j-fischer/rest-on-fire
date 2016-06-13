package org.restonfire;

import org.jdeferred.Promise;
import org.restonfire.exceptions.FirebaseRuntimeException;

/**
 * A {@link FirebaseRestQuery} is create by a {@link FirebaseRestReference} and represents the same location as the
 * creating REST reference. It allows for querying the location with the various different sort and filter
 * mechanisms that Firebase supports.<br>
 * <br>
 * For a full understanding of the behaviour of this class, please visit
 * <a href="https://firebase.google.com/docs/database/web/retrieve-data#sort_data">Firebase's documentation</a> for more
 * details. <br>
 * <a href="https://www.firebase.com/docs/rest/guide/retrieving-data.html">Firebase's old documentation</a> is also a
 * great resource to understand the behaviour of the supported methods.
 *
 * @see <a href="https://firebase.google.com/docs/database/rest/retrieve-data#section-rest-filtering">Firebase Filtering Data Documentation</a>
 */
public interface FirebaseRestQuery {

  /**
   * Return items greater than or equal to the specified key, value, or priority, depending on the order-by method chosen.
   * All values less (or before) the given value will be removed from the result.
   *
   * @param value The starting value of the result set.
   * @return This {@link FirebaseRestQuery} reference.
   * @see <a href="https://firebase.google.com/docs/database/rest/retrieve-data#section-complex-queries">Firebase Complex Filtering Documentation</a>
   */
  FirebaseRestQuery startAt(Object value);

  /**
   * Return items less than or equal to the specified key, value, or priority, depending on the order-by method chosen.
   * All values greater (or after) the given value will be removed from the result.
   *
   * @param value The ending value of the result set.
   * @return This {@link FirebaseRestQuery} reference.
   * @see <a href="https://firebase.google.com/docs/database/rest/retrieve-data#section-complex-queries">Firebase Complex Filtering Documentation</a>
   */
  FirebaseRestQuery endAt(Object value);

  /**
   * Return items equal to the specified key, value, or priority, depending on the order-by method chosen.
   * All other values will be removed from the result.
   *
   * @param value The value all results must be equal to.
   * @return This {@link FirebaseRestQuery} reference.
   * @see <a href="https://firebase.google.com/docs/database/rest/retrieve-data#section-complex-queries">Firebase Complex Filtering Documentation</a>
   */
  FirebaseRestQuery equalTo(Object value);

  /**
   * Sets the maximum number of items to return from the beginning of the ordered list of results.
   * All other values will be removed from the result.
   *
   * @param number Number of results to be returned.
   * @return This {@link FirebaseRestQuery} reference.
   * @see <a href="https://firebase.google.com/docs/database/rest/retrieve-data#section-complex-queries">Firebase Complex Filtering Documentation</a>
   */
  FirebaseRestQuery limitToFirst(int number);

  /**
   * Sets the maximum number of items to return from the end of the ordered list of results.
   * All other values will be removed from the result.
   *
   * @param number Number of results to be returned.
   * @return This {@link FirebaseRestQuery} reference.
   * @see <a href="https://firebase.google.com/docs/database/rest/retrieve-data#section-complex-queries">Firebase Complex Filtering Documentation</a>
   */
  FirebaseRestQuery limitToLast(int number);

  /**
   * Order results by child keys. This is particularly useful for
   * non-generated keys.
   *
   * @return This {@link FirebaseRestQuery} reference.
   * @see <a href="https://firebase.google.com/docs/database/rest/retrieve-data#orderbykey">Firebase Order By Key Documentation</a>
   */
  FirebaseRestQuery orderByKey();

  /**
   * Order results by the value of a specified child name.
   *
   * @return This {@link FirebaseRestQuery} reference.
   * @see <a href="https://firebase.google.com/docs/database/rest/retrieve-data#orderby">Firebase Order By Documentation</a>
   */
  FirebaseRestQuery orderByChild(String name);

  /**
   * Order results by the assigned priority.
   *
   * @return This {@link FirebaseRestQuery} reference.
   * @see <a href="https://firebase.google.com/docs/database/rest/retrieve-data#orderbyvalue">Firebase Order By Priority Documentation</a>
   */
  FirebaseRestQuery orderByPriority();

  /**
   * Order results by child values.
   *
   * @return This {@link FirebaseRestQuery} reference.
   * @see <a href="https://firebase.google.com/docs/database/rest/retrieve-data#orderbyvalue">Firebase Order By Value Documentation</a>
   */
  FirebaseRestQuery orderByValue();

  /**
   * Removes all previously provided sorting and filtering values. This resets the query and alternative values
   * can now be used.
   */
  void clear();

  /**
   * Executes the query with the previously provided sorting and filtering arguments. The function can be invoked
   * multiple times to re-run the same query over and over again.<br>
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
  <T> Promise<T, FirebaseRuntimeException, Void> run(Class<T> clazz);
}
