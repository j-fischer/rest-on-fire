package org.restonfire;

/**
 * A factory to create {@link FirebaseRestNamespace} instances. The implementation will
 * take care of the configuration of the {@link com.ning.http.client.AsyncHttpClient} and
 * {@link com.google.gson.Gson}.
 */
public interface FirebaseRestNamespaceFactory {

  /**
   * Factory method to create a new instance of a {@link FirebaseRestNamespace} for the given URL, using the
   * provided access token as the <code>auth</code> query parameter.
   *
   * @param namespaceUrl The URL root URL to the Firebase namespace, i.e. https://myinstance.firebaseio.com
   * @param accessToken The access token to be used for all requests to this namespace. Value can be <code>null</code>
   * @return A new instance of {@link FirebaseRestNamespace} representing the namespace of the given URL
   */
  FirebaseRestNamespace create(String namespaceUrl, String accessToken);
}
