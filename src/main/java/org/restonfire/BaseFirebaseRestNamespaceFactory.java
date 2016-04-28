package org.restonfire;

import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;

/**
 * {@link FirebaseRestNamespace} implementation using {@link AsyncHttpClient} as the HTTP
 * transport library. This factory also requires the Firebase access token to be provided
 * for the creation.
 */
public final class BaseFirebaseRestNamespaceFactory implements FirebaseRestNamespace {

  /**
   * Creates a FirebaseRestNamespace instance for the given namespace URL.
   *
   * @param asyncHttpClient {@link AsyncHttpClient} instance, which will be used for the HTTP requests.
   * @param gson {@link Gson} instance used for deserialization of all reference responses.
   * @param namespaceUrl A {@link String} representing the Firebase namespace, i.e. https://myinstance.firebaseio.com
   * @param firebaseAccessToken The access token (or secret) for that Firebase namespace. Value can be <code>null</code>
   *
   * @return An instance of a namespace representation for the given namespace URL.
   */
  public static FirebaseRestNamespace create(
    AsyncHttpClient asyncHttpClient,
    Gson gson,
    String namespaceUrl,
    String firebaseAccessToken) {

    return new BaseFirebaseRestNamespaceFactory(asyncHttpClient, gson, namespaceUrl, firebaseAccessToken);
  }

  private final AsyncHttpClient asyncHttpClient;
  private final Gson gson;
  private final String namespaceUrl;
  private final String firebaseAccessToken;

  private BaseFirebaseRestNamespaceFactory(
    AsyncHttpClient asyncHttpClient,
    Gson gson,
    String namespaceUrl,
    String firebaseAccessToken
  ) {
    this.asyncHttpClient = asyncHttpClient;
    this.gson = gson;
    this.namespaceUrl = namespaceUrl;
    this.firebaseAccessToken = firebaseAccessToken;
  }

  @Override
  public FirebaseRestReference getReference(String path) {
    return new FirebaseRestReferenceImpl(
      asyncHttpClient,
      gson,
      namespaceUrl,
      firebaseAccessToken,
      path
    );
  }
}
