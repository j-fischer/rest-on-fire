package org.restonfire;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.client.uri.Uri;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jmock.Expectations;
import org.junit.Test;
import org.restonfire.exceptions.FirebaseAccessException;
import org.restonfire.exceptions.FirebaseRestException;
import org.restonfire.exceptions.FirebaseRuntimeException;
import org.restonfire.responses.FirebaseSecurityRules;
import org.restonfire.testutils.AbstractMockTestCase;
import org.restonfire.testutils.MockObjectHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests for FirebaseSecurityRulesReferenceImpl.
 */
public class FirebaseSecurityRulesReferenceImplTest extends AbstractMockTestCase {

  private final AsyncHttpClient asyncHttpClient = mock(AsyncHttpClient.class);
  private final AsyncHttpClient.BoundRequestBuilder requestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);

  private final Gson gson = new GsonBuilder().create();
  private final String fbBaseUrl = "https://mynamespace.firebaseio.com";

  private final MutableObject<AsyncCompletionHandler<Void>> capturedCompletionHandler = new MutableObject<>();

  private final FirebaseSecurityRulesReferenceImpl ref = new FirebaseSecurityRulesReferenceImpl(
    asyncHttpClient,
    fbBaseUrl,
    null
  );

  @Test
  public void testGet_forbidden() throws Exception {
    expectGetRequest();
    executedForbiddenRequestTest(ref.get());
  }

  @Test
  public void testGet_unauthorized() throws Exception {
    expectGetRequest();
    executedUnauthorizedRequestTest(ref.get());
  }

  @Test
  public void testGetValue_unsupportedStatusCode() throws Exception {
    expectGetRequest();
    executedRequestWithUnsupportedResponseTest(ref.get(), HttpURLConnection.HTTP_GATEWAY_TIMEOUT);

    expectGetRequest();
    executedRequestWithUnsupportedResponseTest(ref.get(), HttpURLConnection.HTTP_INTERNAL_ERROR);

    expectGetRequest();
    executedRequestWithUnsupportedResponseTest(ref.get(), HttpURLConnection.HTTP_NOT_FOUND);
  }

  @Test
  public void testGet_success() throws Exception {
    final Map<String, Object> sampleRules = getSampleRules();

    final FirebaseSecurityRules expectedSecurityRules = new FirebaseSecurityRules(
      sampleRules
    );

    expectGetRequest();

    Promise<FirebaseSecurityRules, FirebaseRuntimeException, Void> result = ref.get();

    result.then(new DoneCallback<FirebaseSecurityRules>() {
      @Override
      public void onDone(FirebaseSecurityRules result) {
        assertEquals(sampleRules.get(".read"), result.getRules().get(".read"));
        assertEquals(sampleRules.get(".write"), result.getRules().get(".write"));
      }
    }).fail(new FailCallback<FirebaseRuntimeException>() {
      @Override
      public void onFail(FirebaseRuntimeException result) {
        fail("The promise should not have been rejected");
      }
    });

    Response response = createResponse(getFirebaseRestUrl(), HttpURLConnection.HTTP_OK, gson.toJson(expectedSecurityRules));

    capturedCompletionHandler.getValue().onCompleted(response);
  }

  private void executedRequestWithUnsupportedResponseTest(Promise<FirebaseSecurityRules, FirebaseRuntimeException, Void> result, int responseCode) throws Exception {
    executedFailedRequestTest(result, responseCode, FirebaseRestException.class, null, FirebaseRuntimeException.ErrorCode.UnsupportedStatusCode);
  }

  private void executedForbiddenRequestTest(Promise<FirebaseSecurityRules, FirebaseRuntimeException, Void> result) throws Exception {
    executedFailedRequestTest(result, HttpURLConnection.HTTP_FORBIDDEN, FirebaseAccessException.class, null, FirebaseRuntimeException.ErrorCode.AccessViolation);
  }

  private void executedUnauthorizedRequestTest(Promise<FirebaseSecurityRules, FirebaseRuntimeException, Void> result) throws Exception {
    executedFailedRequestTest(result, HttpURLConnection.HTTP_UNAUTHORIZED, FirebaseAccessException.class, null, FirebaseRuntimeException.ErrorCode.AccessViolation);
  }

  private <FirebaseSecurityRules, TException extends FirebaseRuntimeException> void executedFailedRequestTest(Promise<FirebaseSecurityRules, FirebaseRuntimeException, Void> result, int statusCode, final Class<TException> exceptionClazz, String requestBody, final FirebaseRuntimeException.ErrorCode expectedErrorCode) throws Exception {
    result.then(new DoneCallback<FirebaseSecurityRules>() {
      @Override
      public void onDone(FirebaseSecurityRules result) {
        fail("The promise should not have been resolved");
      }
    }).fail(new FailCallback<FirebaseRuntimeException>() {
      @Override
      public void onFail(FirebaseRuntimeException result) {
        assertEquals(exceptionClazz, result.getClass());
        assertEquals(expectedErrorCode, result.getErrorCode());
      }
    });

    Response response = createResponse(getFirebaseRestUrl(), statusCode, requestBody);

    capturedCompletionHandler.getValue().onCompleted(response);

    assertIsSatisfied();
  }

  private <T> void expectSetRequest(final T data) {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).preparePut(getFirebaseRestUrl()); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setBody(gson.toJson(data)); will(returnValue(requestBuilder));
      oneOf(requestBuilder).execute(with(aNonNull(AsyncCompletionHandler.class))); will(MockObjectHelper.capture(capturedCompletionHandler));
    }});
  }

  private void expectGetRequest() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).prepareGet(getFirebaseRestUrl()); will(returnValue(requestBuilder));
      oneOf(requestBuilder).execute(with(aNonNull(AsyncCompletionHandler.class))); will(MockObjectHelper.capture(capturedCompletionHandler));
    }});
  }

  private Response createResponse(final String url, final int statusCode, final String responseBody) throws IOException {
    final Response response = mock(Response.class, String.format("Response(%s, %d)", url, statusCode));
    addExpectations(new Expectations() {{
      allowing(response).getUri(); will(returnValue(Uri.create(url)));
      allowing(response).getStatusCode(); will(returnValue(statusCode));
      allowing(response).getResponseBody(); will(returnValue(responseBody));
    }});

    return response;
  }

  private Map<String, Object> getSampleRules() {
    HashMap<String, Object> rules = new HashMap<>();
    rules.put(".read", true);
    rules.put(".write", false);

    return rules;
  }

  private String getFirebaseRestUrl() {
    return fbBaseUrl + "/.settings/rules" + FirebaseDocumentLocation.JSON_SUFFIX;
  }
}
