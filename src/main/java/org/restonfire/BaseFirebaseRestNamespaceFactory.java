package org.restonfire;

import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;

/**
 * {@link FirebaseRestNamespace} implementation.
 *
 * Created by jfischer on 2016-04-07.
 */
public final class BaseFirebaseRestNamespaceFactory implements FirebaseRestNamespace {

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
