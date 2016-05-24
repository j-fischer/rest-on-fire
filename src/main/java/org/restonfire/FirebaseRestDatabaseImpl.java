package org.restonfire;

import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FirebaseRestDatabase} implementation.
 */
class FirebaseRestDatabaseImpl implements FirebaseRestDatabase {

  private static final Logger LOG = LoggerFactory.getLogger(FirebaseRestDatabaseImpl.class);

  private final AsyncHttpClient asyncHttpClient;
  private final Gson gson;
  private final String namespaceUrl;
  private final String firebaseAccessToken;

  FirebaseRestDatabaseImpl(
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

  @Override
  public FirebaseSecurityRulesReference getSecurityRules() {
    LOG.info("Creating new FirebaseSecurityRulesReference");

    // FirebaseSecurityRulesReferenceImpl has its own Gson instance, in order to apply special configuration settings
    return new FirebaseSecurityRulesReferenceImpl(
      asyncHttpClient,
      namespaceUrl,
      firebaseAccessToken
    );
  }
}
