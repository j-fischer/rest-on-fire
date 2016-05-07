package org.restonfire;

import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FirebaseRestNamespace} implementation.
 */
class FirebaseRestNamespaceImpl implements FirebaseRestNamespace {

  private static final Logger LOG = LoggerFactory.getLogger(FirebaseRestNamespaceImpl.class);

  private final AsyncHttpClient asyncHttpClient;
  private final Gson gson;
  private final String namespaceUrl;
  private final String firebaseAccessToken;

  FirebaseRestNamespaceImpl(
    AsyncHttpClient asyncHttpClient,
    Gson gson,
    String namespaceUrl,
    String firebaseAccessToken
  ) {
    this.asyncHttpClient = asyncHttpClient;
    this.gson = gson;
    this.namespaceUrl = PathUtil.normalizePath(namespaceUrl);
    this.firebaseAccessToken = firebaseAccessToken;
  }

  @Override
  public FirebaseRestReference getReference(String path) {
    LOG.info("Creating new FirebaseRestReference for path '{}'", path);

    return new FirebaseRestReferenceImpl(
      asyncHttpClient,
      gson,
      namespaceUrl,
      firebaseAccessToken,
      path
    );
  }

  @Override
  public FirebaseRestEventStream getEventStream(String path) {
    LOG.info("Creating new FirebaseEventStream for path '{}'", path);

    return new FirebaseRestEventStreamImpl(
      asyncHttpClient,
      gson,
      namespaceUrl,
      firebaseAccessToken,
      path
    );
  }
}
