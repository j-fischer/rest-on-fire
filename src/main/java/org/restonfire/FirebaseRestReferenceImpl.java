package org.restonfire;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.restonfire.exceptions.FirebaseAccessException;
import org.restonfire.exceptions.FirebaseRestException;
import org.restonfire.exceptions.FirebaseRuntimeException;
import org.restonfire.responses.PushResponse;
import org.restonfire.utils.PathUtil;
import org.restonfire.utils.RequestBuilderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * {@link FirebaseRestReference} implementation.
 *
 * Created by jfischer on 2016-04-14.
 */
final class FirebaseRestReferenceImpl implements FirebaseRestReference {

  private static final Logger LOG = LoggerFactory.getLogger(FirebaseRestReferenceImpl.class);

  private static final String FAILED_TO_PARSE_RESPONSE_BODY_FOR_REQUEST = "Failed to parse responses body for request: ";

  private final Gson gson;
  private final AsyncHttpClient asyncHttpClient;

  private final String path;
  private final String fbBaseUrl;
  private final String fbAccessToken;
  private final String referenceUrl;

  FirebaseRestReferenceImpl(
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
  public String getReferenceUrl() {
    return referenceUrl;
  }

  @Override
  public <T> Promise<T, FirebaseRuntimeException, Void> getValue(final Class<T> clazz) {
    LOG.debug("getValue({}) invoked for reference {}", clazz, referenceUrl);
    final Deferred<T, FirebaseRuntimeException, Void> deferred = new DeferredObject<>();

    final AsyncHttpClient.BoundRequestBuilder getRequest = RequestBuilderUtil.createGet(asyncHttpClient, referenceUrl);

    getRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        try {
          LOG.debug("Request for getValue({}) completed", clazz);
          final T result = handleResponse(response, clazz);
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
    LOG.debug("setValue({}) invoked for reference {}", value, referenceUrl);
    final Deferred<T, FirebaseRuntimeException, Void> deferred = new DeferredObject<>();

    final AsyncHttpClient.BoundRequestBuilder putRequest = RequestBuilderUtil.createPut(asyncHttpClient, referenceUrl, gson.toJson(value));

    putRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        LOG.debug("Request for setValue({}) completed for reference {}", value, referenceUrl);
        return handleValueModifiedResponse(response, deferred, value);
      }
    });

    return deferred.promise();
  }

  @Override
  public <T> Promise<T, FirebaseRuntimeException, Void> updateValue(final T value) {
    LOG.debug("updateValue({}) invoked for reference {}", value, referenceUrl);
    final Deferred<T, FirebaseRuntimeException, Void> deferred = new DeferredObject<>();

    final AsyncHttpClient.BoundRequestBuilder patchRequest = RequestBuilderUtil.createPatch(asyncHttpClient, referenceUrl, gson.toJson(value));

    patchRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        LOG.debug("Request for updateValue({}) completed for reference {}", value, referenceUrl);
        return handleValueModifiedResponse(response, deferred, value);
      }
    });

    return deferred.promise();
  }

  @Override
  public Promise<Void, FirebaseRuntimeException, Void> removeValue() {
    LOG.debug("removeValue() invoked for reference {}", referenceUrl);
    final Deferred<Void, FirebaseRuntimeException, Void> deferred = new DeferredObject<>();

    final AsyncHttpClient.BoundRequestBuilder deleteRequest = RequestBuilderUtil.createDelete(asyncHttpClient, referenceUrl);

    deleteRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        LOG.debug("Request for removeValue() completed for reference {}", referenceUrl);
        return handleValueModifiedResponse(response, deferred, null);
      }
    });

    return deferred.promise();
  }

  @Override
  public Promise<FirebaseRestReference, FirebaseRuntimeException, Void> push() {
    LOG.debug("push() invoked for reference {}", referenceUrl);
    final Deferred<FirebaseRestReference, FirebaseRuntimeException, Void> deferred = new DeferredObject<>();

    final AsyncHttpClient.BoundRequestBuilder postRequest = RequestBuilderUtil.createPost(asyncHttpClient, referenceUrl, "{}");

    postRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        return handleNewReferenceCreatedResponse(response, deferred);
      }
    });

    return deferred.promise();
  }

  @Override
  public FirebaseRestReference getRoot() {
    LOG.debug("getRoot() invoked for reference {}", referenceUrl);
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
    LOG.debug("getParent() invoked for reference {}", referenceUrl);
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
    LOG.debug("child({}) invoked for reference {}", childPath, referenceUrl);
    return new FirebaseRestReferenceImpl(
      asyncHttpClient,
      gson,
      fbBaseUrl,
      fbAccessToken,
      PathUtil.getChild(path, childPath)
    );
  }

  private <T> Void handleValueModifiedResponse(Response response, Deferred<T, FirebaseRuntimeException, Void> deferred, T value) {
    try {
      handleResponse(response, null);
      deferred.resolve(value);
    } catch (FirebaseRuntimeException ex) {
      deferred.reject(ex);
    }
    return null;
  }

  private Void handleNewReferenceCreatedResponse(Response response, Deferred<FirebaseRestReference, FirebaseRuntimeException, Void> deferred) throws IOException {
    try {
      // Note: push() is currently the only function calling handleNewReferenceCreatedResponse
      LOG.debug("Request for push() completed for reference {}", referenceUrl);

      final PushResponse pushResponse = gson.fromJson(response.getResponseBody(), PushResponse.class);

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
          return clazz == null
            ? null
            : gson.fromJson(response.getResponseBody(), clazz);
        case HttpURLConnection.HTTP_FORBIDDEN:
          LOG.warn("The request to '{}' that violates the Security and Firebase Rules", referenceUrl);
          throw new FirebaseAccessException(response);
        default:
          LOG.error("Unsupported status code: " + response.getStatusCode());
          throw new FirebaseRestException(response);
      }
    } catch (JsonSyntaxException | IOException e) {
      LOG.error(FAILED_TO_PARSE_RESPONSE_BODY_FOR_REQUEST + response.getUri(), e);
      throw new FirebaseRestException(FAILED_TO_PARSE_RESPONSE_BODY_FOR_REQUEST + response.getUri(), e);
    }
  }
}
