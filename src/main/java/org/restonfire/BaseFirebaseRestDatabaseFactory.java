package org.restonfire;

import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FirebaseRestDatabaseFactory} implementation using {@link AsyncHttpClient} as the HTTP
 * transport library. This factory also requires the Firebase access token to be provided
 * for the creation.
 */
public final class BaseFirebaseRestDatabaseFactory implements FirebaseRestDatabaseFactory {

  private static final Logger LOG = LoggerFactory.getLogger(BaseFirebaseRestDatabaseFactory.class);

  private final AsyncHttpClient asyncHttpClient;
  private final Gson gson;

  /**
   * Base factory which requires the {@link AsyncHttpClient} and {@link Gson} dependencies to be injected.
   *
   * @param asyncHttpClient {@link AsyncHttpClient} instance, which will be used for the HTTP requests.
   * @param gson {@link Gson} instance used for deserialization of all reference responses.
   */
  public BaseFirebaseRestDatabaseFactory(
    AsyncHttpClient asyncHttpClient,
    Gson gson
  ) {
    this.asyncHttpClient = asyncHttpClient;
    this.gson = gson;
  }

  @Override
  public FirebaseRestDatabase create(
    String databaseUrl,
    String firebaseAccessToken) {

    LOG.info("Creating FirebaseRestDatabase for url '{}' {} accessToken",
      databaseUrl,
      StringUtil.notNullOrEmpty(firebaseAccessToken) ? "with" : "without"
    );

    return new FirebaseRestDatabaseImpl(asyncHttpClient, gson, databaseUrl, firebaseAccessToken);
  }
}
