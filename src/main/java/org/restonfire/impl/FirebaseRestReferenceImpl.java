package org.restonfire.impl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.restonfire.FirebaseRestReference;
import org.restonfire.exceptions.FirebaseAccessException;
import org.restonfire.exceptions.FirebaseRestException;
import org.restonfire.exceptions.FirebaseRuntimeException;
import org.restonfire.response.PushResponse;
import org.restonfire.utils.PathUtil;
import org.restonfire.utils.RequestExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.Future;

/**
 * Created by jfischer on 2016-04-14.
 */
public class FirebaseRestReferenceImpl implements FirebaseRestReference {

  private static final Logger LOG = LoggerFactory.getLogger(FirebaseRestReferenceImpl.class);

  private final Gson gson;
  private final AsyncHttpClient asyncHttpClient;

  private final String path;
  private final String fbBaseUrl;
  private final String fbAccessToken;
  private final String referenceUrl;

  public FirebaseRestReferenceImpl(
    AsyncHttpClient asyncHttpClient,
    Gson gson,
    String fbBaseUrl,
    String fbAccessToken,
    String path) {

    this.gson = gson;
    this.asyncHttpClient = asyncHttpClient;
    this.fbBaseUrl = fbBaseUrl;
    this.fbAccessToken = fbAccessToken;
    this.path = path;

    this.referenceUrl = fbBaseUrl + path;
  }


  @Override
  public <T> Promise<T, FirebaseRuntimeException, Void> getValue(final Class<T> clazz) {
    final Deferred deferred = new DeferredObject();

    AsyncHttpClient.BoundRequestBuilder getRequest = RequestExecutorUtil.createGet(asyncHttpClient, referenceUrl);

    getRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        handleResponse(deferred, response, clazz);
        return null;
      }
    });

    return deferred.promise();
  }

  @Override
  public void setValue(Object value) {

  }

  @Override
  public void updateValue(Object value) {
    AsyncHttpClient.BoundRequestBuilder getRequest = RequestExecutorUtil.createPatch(asyncHttpClient, referenceUrl, gson.toJson(value));

    AsyncCompletionHandler<FirebaseRestReference> handler = new AsyncCompletionHandler<FirebaseRestReference>() {

      @Override
      public FirebaseRestReference onCompleted(Response response) throws Exception {
        PushResponse pushResponse = gson.fromJson(response.getResponseBody(), PushResponse.class);

        return new FirebaseRestReferenceImpl(
          asyncHttpClient,
          gson,
          fbBaseUrl,
          fbAccessToken,
          PathUtil.getChild(path, pushResponse.getName())
        );
      }
    };

    getRequest.execute(handler);
  }

  @Override
  public <T> void removeValue(T value) {

  }

  @Override
  public Future<FirebaseRestReference> push() {
    AsyncHttpClient.BoundRequestBuilder getRequest = RequestExecutorUtil.createPost(asyncHttpClient, referenceUrl, "{}");

    AsyncCompletionHandler<FirebaseRestReference> handler = new AsyncCompletionHandler<FirebaseRestReference>() {

      @Override
      public FirebaseRestReference onCompleted(Response response) throws Exception {
        PushResponse pushResponse = gson.fromJson(response.getResponseBody(), PushResponse.class);

        return new FirebaseRestReferenceImpl(
          asyncHttpClient,
          gson,
          fbBaseUrl,
          fbAccessToken,
          PathUtil.getChild(path, pushResponse.getName())
        );
      }
    };

    return getRequest.execute(handler);
  }

  @Override
  public FirebaseRestReference root() {
    return new FirebaseRestReferenceImpl(
      asyncHttpClient,
      gson,
      fbBaseUrl,
      fbAccessToken,
      ""
    );
  }

  @Override
  public FirebaseRestReference parent() {
    return new FirebaseRestReferenceImpl(
      asyncHttpClient,
      gson,
      fbBaseUrl,
      fbAccessToken,
      PathUtil.getParent(path)
    );
  }

  @Override
  public FirebaseRestReference child(String childPath) {
    return new FirebaseRestReferenceImpl(
      asyncHttpClient,
      gson,
      fbBaseUrl,
      fbAccessToken,
      PathUtil.getChild(path, childPath)
    );
  }

  private <T> void handleResponse(Deferred deferred, Response response, Class<T> clazz) {
    try {
      switch (response.getStatusCode()) {
        case HttpURLConnection.HTTP_OK:
          deferred.resolve(gson.fromJson(response.getResponseBody(), clazz));
        case HttpURLConnection.HTTP_FORBIDDEN:
          LOG.warn("The request to '{}' that violates the Security and Firebase Rules", referenceUrl);
          deferred.reject(new FirebaseAccessException(response));
        default:
          LOG.error("Unsupported status code: " + response.getStatusCode());
          deferred.reject(new FirebaseRestException(response));
      }
    } catch (JsonSyntaxException | IOException e) {
      LOG.error("Failed to parse response body for request: " + response.getUri(), e);
      deferred.reject(new FirebaseRestException("Failed to parse response body for request: " + response.getUri(), e));
    }
  }
}
