package org.restonfire;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.restonfire.exceptions.FirebaseRuntimeException;
import org.restonfire.responses.FirebaseSecurityRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FirebaseSecurityRulesReference} implementation.
 */
class FirebaseSecurityRulesReferenceImpl implements FirebaseSecurityRulesReference {

  private static final Logger LOG = LoggerFactory.getLogger(FirebaseSecurityRulesReferenceImpl.class);

  private final Gson gson = new GsonBuilder()
    .setPrettyPrinting()
    .disableHtmlEscaping()
    .create();

  private final AsyncHttpClient asyncHttpClient;

  private final String fbAccessToken;
  private final String referenceUrl;

  FirebaseSecurityRulesReferenceImpl(
    AsyncHttpClient asyncHttpClient,
    String fbBaseUrl,
    String fbAccessToken
  ) {
    this.asyncHttpClient = asyncHttpClient;
    this.fbAccessToken = fbAccessToken;
    this.referenceUrl = PathUtil.concatenatePath(fbBaseUrl, ".settings/rules") + FirebaseDocumentLocation.JSON_SUFFIX;
  }

  @Override
  public Promise<FirebaseSecurityRules, FirebaseRuntimeException, Void> get() {
    LOG.debug("getValue() invoked for security rules");
    final Deferred<FirebaseSecurityRules, FirebaseRuntimeException, Void> deferred = new DeferredObject<>();

    final AsyncHttpClient.BoundRequestBuilder getRequest = RequestBuilderUtil.createGet(asyncHttpClient, referenceUrl, fbAccessToken);

    getRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        try {
          LOG.debug("Request for getValue() completed");
          final FirebaseSecurityRules result = handleResponse(response);
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
  public Promise<FirebaseSecurityRules, FirebaseRuntimeException, Void> set(final FirebaseSecurityRules newRules) {
    LOG.debug("setValue() invoked for security rules");
    final Deferred<FirebaseSecurityRules, FirebaseRuntimeException, Void> deferred = new DeferredObject<>();

    final AsyncHttpClient.BoundRequestBuilder putRequest = RequestBuilderUtil.createPut(asyncHttpClient, referenceUrl, fbAccessToken, gson.toJson(newRules));

    putRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        LOG.debug("Request for setValue() completed");
        return handleValueModifiedResponse(response, deferred, newRules);
      }
    });

    return deferred.promise();
  }

  private Void handleValueModifiedResponse(Response response, Deferred<FirebaseSecurityRules, FirebaseRuntimeException, Void> deferred, FirebaseSecurityRules value) {
    try {
      handleResponse(response);
      deferred.resolve(value);
    } catch (FirebaseRuntimeException ex) {
      deferred.reject(ex);
    }
    return null;
  }

  private FirebaseSecurityRules handleResponse(Response response) {
    return RestUtil.handleResponse(gson, referenceUrl, response, FirebaseSecurityRules.class);
  }
}
