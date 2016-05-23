package org.restonfire;

/**
 * A factory to create {@link FirebaseRestDatabase} instances. The implementation will
 * take care of the configuration of the {@link com.ning.http.client.AsyncHttpClient} and
 * {@link com.google.gson.Gson}.
 */
public interface FirebaseRestDatabaseFactory {

  /**
   * Factory method to create a new instance of a {@link FirebaseRestDatabase} for the given URL, using the
   * provided access token as the <code>auth</code> query parameter.
   *
   * @param databaseUrl The URL root URL to the Firebase database, i.e. https://myinstance.firebaseio.com
   * @param accessToken The access token to be used for all requests to this database. Value can be <code>null</code>
   * @return A new instance of {@link FirebaseRestDatabase} representing the database of the given URL
   */
  FirebaseRestDatabase create(String databaseUrl, String accessToken);
}
