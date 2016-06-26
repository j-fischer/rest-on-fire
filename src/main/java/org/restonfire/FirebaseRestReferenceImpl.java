package org.restonfire;

import com.google.gson.Gson;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.restonfire.exceptions.FirebaseRuntimeException;
import org.restonfire.responses.PushResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * {@link FirebaseRestReference} implementation.
 */
final class FirebaseRestReferenceImpl extends FirebaseDocumentLocation implements FirebaseRestReference {

  private static final Logger LOG = LoggerFactory.getLogger(FirebaseRestReferenceImpl.class);

  private static final String PRIORITY_PATH = ".priority";

  private final Gson gson;
  private final AsyncHttpClient asyncHttpClient;

  FirebaseRestReferenceImpl(
    AsyncHttpClient asyncHttpClient,
    Gson gson,
    String fbBaseUrl,
    String fbAccessToken,
    String path) {

    super(fbBaseUrl, path, fbAccessToken);

    this.gson = gson;
    this.asyncHttpClient = asyncHttpClient;
  }

  @Override
  public <T> Promise<T, FirebaseRuntimeException, Void> getValue(final Class<T> clazz) {
    LOG.debug("getValue({}) invoked for reference {}", clazz, referenceUrl);
    final Deferred<T, FirebaseRuntimeException, Void> deferred = new DeferredObject<>();

    final AsyncHttpClient.BoundRequestBuilder getRequest = RequestBuilderUtil.createGet(asyncHttpClient, referenceUrl, fbAccessToken);

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
  public Promise<Object, FirebaseRuntimeException, Void> getShallowValue() {
    LOG.debug("getShallowValue() invoked for reference {}", referenceUrl);
    final Deferred<Object, FirebaseRuntimeException, Void> deferred = new DeferredObject<>();

    final AsyncHttpClient.BoundRequestBuilder getRequest = RequestBuilderUtil.createGet(asyncHttpClient, referenceUrl, fbAccessToken);
    getRequest.addQueryParam("shallow", "true");

    getRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        try {
          LOG.debug("Request for getShallowValue() completed");
          final Object result = handleResponse(response, Object.class);
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

    final AsyncHttpClient.BoundRequestBuilder putRequest = RequestBuilderUtil.createPut(asyncHttpClient, referenceUrl, fbAccessToken, gson.toJson(value));

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

    final AsyncHttpClient.BoundRequestBuilder patchRequest = RequestBuilderUtil.createPatch(asyncHttpClient, referenceUrl, fbAccessToken, gson.toJson(value));

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

    final AsyncHttpClient.BoundRequestBuilder deleteRequest = RequestBuilderUtil.createDelete(asyncHttpClient, referenceUrl, fbAccessToken);

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

    final AsyncHttpClient.BoundRequestBuilder postRequest = RequestBuilderUtil.createPost(asyncHttpClient, referenceUrl, fbAccessToken, "{}");

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
      PathUtil.concatenatePath(path, childPath)
    );
  }

  @Override
  public FirebaseRestQuery query() {
    return new FirebaseRestQueryImpl(
      gson,
      RequestBuilderUtil.createGet(asyncHttpClient, referenceUrl, fbAccessToken),
      referenceUrl
    );
  }

  @Override
  public Promise<Void, FirebaseRuntimeException, Void> setPriority(final double priority) {
    LOG.debug("setPriority({}) invoked for reference {}", priority, referenceUrl);
    final Deferred<Void, FirebaseRuntimeException, Void> deferred = new DeferredObject<>();

    final String priorityUrl = PathUtil.concatenatePath(getReferenceUrl(), PRIORITY_PATH) + JSON_SUFFIX;
    final AsyncHttpClient.BoundRequestBuilder putRequest = RequestBuilderUtil.createPut(asyncHttpClient, priorityUrl, fbAccessToken, gson.toJson(priority));

    putRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        LOG.debug("Request for setPriority({}) completed for reference {}", priority, referenceUrl);
        return handleValueModifiedResponse(response, deferred, null);
      }
    });

    return deferred.promise();
  }

  @Override
  public Promise<Double, FirebaseRuntimeException, Void> getPriority() {
    LOG.debug("getPriority() invoked for reference {}", referenceUrl);
    final Deferred<Double, FirebaseRuntimeException, Void> deferred = new DeferredObject<>();

    final String priorityUrl = PathUtil.concatenatePath(getReferenceUrl(), PRIORITY_PATH) + JSON_SUFFIX;
    final AsyncHttpClient.BoundRequestBuilder getRequest = RequestBuilderUtil.createGet(asyncHttpClient, priorityUrl, fbAccessToken);

    getRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        try {
          LOG.debug("Request for getPriority() completed");
          final Double result = handleResponse(response, Double.class);
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
  public Promise<Void, FirebaseRuntimeException, Void> removePriority() {
    LOG.debug("removePriority() invoked for reference {}", referenceUrl);
    final Deferred<Void, FirebaseRuntimeException, Void> deferred = new DeferredObject<>();

    final String priorityUrl = PathUtil.concatenatePath(getReferenceUrl(), PRIORITY_PATH) + JSON_SUFFIX;
    final AsyncHttpClient.BoundRequestBuilder deleteRequest = RequestBuilderUtil.createDelete(asyncHttpClient, priorityUrl, fbAccessToken);

    deleteRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        LOG.debug("Request for removePriority() completed for reference {}", referenceUrl);
        return handleValueModifiedResponse(response, deferred, null);
      }
    });

    return deferred.promise();
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

      final PushResponse pushResponse = handleResponse(response, PushResponse.class);

      deferred.resolve(new FirebaseRestReferenceImpl(
        asyncHttpClient,
        gson,
        fbBaseUrl,
        fbAccessToken,
        PathUtil.concatenatePath(path, pushResponse.getName())
      ));
    } catch (FirebaseRuntimeException ex) {
      deferred.reject(ex);
    }

    return null;
  }

  private <T> T handleResponse(Response response, Class<T> clazz) {
    return RestUtil.handleResponse(gson, referenceUrl, response, clazz);
  }
}
