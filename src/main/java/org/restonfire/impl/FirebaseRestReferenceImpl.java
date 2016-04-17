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
import org.restonfire.utils.RequestBuilderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

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
    LOG.debug("getValue({}) invoked", clazz);
    final Deferred deferred = new DeferredObject();

    AsyncHttpClient.BoundRequestBuilder getRequest = RequestBuilderUtil.createGet(asyncHttpClient, referenceUrl);

    getRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        try {
          T result = handleResponse(response, clazz);
          deferred.resolve(result);
        } catch (FirebaseRuntimeException ex) {
          deferred.reject(ex);
        }
        return null;
      }
    });

    return deferred.promise();
  }

  @Override
  public <T> Promise<T, FirebaseRuntimeException, Void> setValue(final T value) {
    LOG.debug("setValue({}) invoked", value);
    final Deferred deferred = new DeferredObject();

    AsyncHttpClient.BoundRequestBuilder getRequest = RequestBuilderUtil.createPut(asyncHttpClient, referenceUrl, gson.toJson(value));

    AsyncCompletionHandler<Void> handler = new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        return handleValueModifiedResponse(response, deferred, value);
      }
    };

    getRequest.execute(handler);

    return deferred.promise();
  }

  @Override
  public <T> Promise<T, FirebaseRuntimeException, Void> updateValue(final T value) {
    LOG.debug("updateValue({}) invoked", value);
    final Deferred deferred = new DeferredObject();

    AsyncHttpClient.BoundRequestBuilder getRequest = RequestBuilderUtil.createPut(asyncHttpClient, referenceUrl, gson.toJson(value));

    AsyncCompletionHandler<Void> handler = new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        return handleValueModifiedResponse(response, deferred, (T) value);
      }
    };

    getRequest.execute(handler);

    return deferred.promise();
  }

  @Override
  public Promise<Void, FirebaseRuntimeException, Void> removeValue() {
    final Deferred deferred = new DeferredObject();

    AsyncHttpClient.BoundRequestBuilder getRequest = RequestBuilderUtil.createDelete(asyncHttpClient, referenceUrl);

    getRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        return handleValueModifiedResponse(response, deferred, null);
      }
    });

    return deferred.promise();
  }

  @Override
  public Promise<FirebaseRestReference, FirebaseRuntimeException, Void> push() {
    final Deferred deferred = new DeferredObject();

    AsyncHttpClient.BoundRequestBuilder getRequest = RequestBuilderUtil.createPost(asyncHttpClient, referenceUrl, "{}");

    AsyncCompletionHandler<Void> handler = new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        return handleNewReferenceCreatedResponse(response, deferred);
      }
    };

    getRequest.execute(handler);

    return deferred.promise();
  }

  @Override
  public FirebaseRestReference getRoot() {
    return new FirebaseRestReferenceImpl(
      asyncHttpClient,
      gson,
      fbBaseUrl,
      fbAccessToken,
      ""
    );
  }

  @Override
  public FirebaseRestReference getParent() {
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

  private <T> Void handleValueModifiedResponse(Response response, Deferred deferred, T value) {
    try {
      handleResponse(response, null);
      deferred.resolve(value);
    } catch (FirebaseRuntimeException ex) {
      deferred.reject(ex);
    }
    return null;
  }

  private Void handleNewReferenceCreatedResponse(Response response, Deferred deferred) throws IOException {
    try {
      PushResponse pushResponse = gson.fromJson(response.getResponseBody(), PushResponse.class);

      deferred.resolve(new FirebaseRestReferenceImpl(
        asyncHttpClient,
        gson,
        fbBaseUrl,
        fbAccessToken,
        PathUtil.getChild(path, pushResponse.getName())
      ));
    } catch (FirebaseRuntimeException ex) {
      deferred.reject(ex);
    }

    return null;
  }

  private <T> T handleResponse(Response response, Class<T> clazz) {
    try {
      switch (response.getStatusCode()) {
        case HttpURLConnection.HTTP_OK:
          return clazz == null ?
            null :
            gson.fromJson(response.getResponseBody(), clazz);
        case HttpURLConnection.HTTP_FORBIDDEN:
          LOG.warn("The request to '{}' that violates the Security and Firebase Rules", referenceUrl);
          throw new FirebaseAccessException(response);
        default:
          LOG.error("Unsupported status code: " + response.getStatusCode());
          throw new FirebaseRestException(response);
      }
    } catch (JsonSyntaxException | IOException e) {
      LOG.error("Failed to parse response body for request: " + response.getUri(), e);
      throw new FirebaseRestException("Failed to parse response body for request: " + response.getUri(), e);
    }
  }
}
