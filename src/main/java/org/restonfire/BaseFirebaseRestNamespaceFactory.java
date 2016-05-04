package org.restonfire;

import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FirebaseRestNamespaceFactory} implementation using {@link AsyncHttpClient} as the HTTP
 * transport library. This factory also requires the Firebase access token to be provided
 * for the creation.
 */
public final class BaseFirebaseRestNamespaceFactory implements FirebaseRestNamespaceFactory {

  private static final Logger LOG = LoggerFactory.getLogger(BaseFirebaseRestNamespaceFactory.class);

  private final AsyncHttpClient asyncHttpClient;
  private final Gson gson;

  /**
   * Base factory which requires the {@link AsyncHttpClient} and {@link Gson} dependencies to be injected.
   *
   * @param asyncHttpClient {@link AsyncHttpClient} instance, which will be used for the HTTP requests.
   * @param gson {@link Gson} instance used for deserialization of all reference responses.
   */
  public BaseFirebaseRestNamespaceFactory(
    AsyncHttpClient asyncHttpClient,
    Gson gson
  ) {
    this.asyncHttpClient = asyncHttpClient;
    this.gson = gson;
  }

  @Override
  public FirebaseRestNamespace create(
    String namespaceUrl,
    String firebaseAccessToken) {

    LOG.info("Creating FirebaseRestNamespace for url '{}' {} accessToken",
      namespaceUrl,
      StringUtil.notNullOrEmpty(firebaseAccessToken) ? "with" : "without"
    );

    return new FirebaseRestNamespaceImpl(asyncHttpClient, gson, namespaceUrl, firebaseAccessToken);
  }
}
