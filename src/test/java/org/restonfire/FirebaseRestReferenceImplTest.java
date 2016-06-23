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
import org.restonfire.responses.PushResponse;
import org.restonfire.testdata.SampleData;
import org.restonfire.testutils.AbstractMockTestCase;
import org.restonfire.testutils.MockObjectHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test class for FirebaseRestReferenceImpl.
 */
public class FirebaseRestReferenceImplTest extends AbstractMockTestCase {

  private final AsyncHttpClient asyncHttpClient = mock(AsyncHttpClient.class);
  private final AsyncHttpClient.BoundRequestBuilder requestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);

  private final Gson gson = new GsonBuilder().create();
  private final String fbBaseUrl = "https://mynamespace.firebaseio.com";
  private final String path = "foo/bar";
  private final String fbReferenceUrl = fbBaseUrl + PathUtil.FORWARD_SLASH + path;
  private final SampleData sampleData = new SampleData("foobar", 123);

  private final MutableObject<AsyncCompletionHandler<Void>> capturedCompletionHandler = new MutableObject<>();

  private final FirebaseRestReferenceImpl ref = new FirebaseRestReferenceImpl(
    asyncHttpClient,
    gson,
    fbBaseUrl,
    null,
    path
  );

  @Test
  public void testGetReferenceUrl() {
    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH + path, ref.getReferenceUrl());
  }

  @Test
  public void testGetRoot() {
    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH, ref.getRoot().getReferenceUrl());
  }

  @Test
  public void testGetParent() {
    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH + "foo", ref.getParent().getReferenceUrl());
  }

  @Test
  public void testChild() {
    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH + path + PathUtil.FORWARD_SLASH + "test", ref.child("test").getReferenceUrl());
    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH + path + PathUtil.FORWARD_SLASH + "test/something", ref.child("test/something/").getReferenceUrl());
  }

  @Test
  public void testGetValue_forbidden() throws Exception {
    expectGetRequest();
    executedForbiddenRequestTest(ref.getValue(SampleData.class));
  }

  @Test
  public void testGetValue_unauthorized() throws Exception {
    expectGetRequest();
    executedUnauthorizedRequestTest(ref.getValue(SampleData.class));
  }

  @Test
  public void testGetValue_unsupportedStatusCode() throws Exception {
    expectGetRequest();
    executedRequestWithUnsupportedResponseTest(ref.getValue(SampleData.class), HttpURLConnection.HTTP_GATEWAY_TIMEOUT);

    expectGetRequest();
    executedRequestWithUnsupportedResponseTest(ref.getValue(SampleData.class), HttpURLConnection.HTTP_INTERNAL_ERROR);

    expectGetRequest();
    executedRequestWithUnsupportedResponseTest(ref.getValue(SampleData.class), HttpURLConnection.HTTP_NOT_FOUND);
  }

  @Test
  public void testGetValue_invalidClassType() throws Exception {
    expectGetRequest();
    executedFailedRequestTest(ref.getValue(SampleData.class), HttpURLConnection.HTTP_OK, FirebaseRestException.class, "{aString: 'abc', anInt: 'foo'}", FirebaseRuntimeException.ErrorCode.ResponseDeserializationFailure);
  }

  @Test
  public void testGetValue_success() throws Exception {
    executeSuccessfulGetValueRequest(ref);
  }

  @Test
  public void testGetValue_success_withAccessToken() throws Exception {
    final String fbAccessToken = "fbAccessToken";
    final FirebaseRestReferenceImpl refWithToken = new FirebaseRestReferenceImpl(
      asyncHttpClient,
      gson,
      fbBaseUrl,
      fbAccessToken,
      path
    );

    addExpectations(new Expectations() {{
      oneOf(requestBuilder).addQueryParam("auth", fbAccessToken); will(returnValue(requestBuilder));
    }});

    executeSuccessfulGetValueRequest(refWithToken);
  }

  @Test
  public void testSetValue_forbidden() throws Exception {
    expectSetRequest(null);

    executedForbiddenRequestTest(ref.setValue((SampleData) null));
  }

  @Test
  public void testSetValue_unauthorized() throws Exception {
    expectSetRequest(null);

    executedUnauthorizedRequestTest(ref.setValue((SampleData) null));
  }

  @Test
  public void testSetValue_unsupportedStatusCode() throws Exception {
    expectSetRequest(sampleData);
    executedRequestWithUnsupportedResponseTest(ref.setValue(sampleData), HttpURLConnection.HTTP_GATEWAY_TIMEOUT);

    expectSetRequest(sampleData);
    executedRequestWithUnsupportedResponseTest(ref.setValue(sampleData), HttpURLConnection.HTTP_INTERNAL_ERROR);

    expectSetRequest(sampleData);
    executedRequestWithUnsupportedResponseTest(ref.setValue(sampleData), HttpURLConnection.HTTP_NOT_FOUND);
  }
  
  @Test
  public void testSetValue_success() throws Exception {
    expectSetRequest(sampleData);

    Promise<SampleData, FirebaseRuntimeException, Void> result = ref.setValue(sampleData);

    result.then(new DoneCallback<SampleData>() {
      @Override
      public void onDone(SampleData result) {
        assertSame(sampleData, result);
      }
    }).fail(new FailCallback<FirebaseRuntimeException>() {
      @Override
      public void onFail(FirebaseRuntimeException result) {
        fail("The promise should not have been rejected");
      }
    });

    Response response = createResponse(fbReferenceUrl, HttpURLConnection.HTTP_OK, gson.toJson(sampleData));

    capturedCompletionHandler.getValue().onCompleted(response);
  }

  @Test
  public void testSetValue_timestamp() throws Exception {
    expectSetRequest(ServerValues.TIMESTAMP);

    Promise<Map<String, String>, FirebaseRuntimeException, Void> result = ref.setValue(ServerValues.TIMESTAMP);

    result.then(new DoneCallback<Map<String, String>>() {
      @Override
      public void onDone(Map<String, String> result) {
        assertSame(ServerValues.TIMESTAMP, result);
      }
    }).fail(new FailCallback<FirebaseRuntimeException>() {
      @Override
      public void onFail(FirebaseRuntimeException result) {
        fail("The promise should not have been rejected");
      }
    });

    Response response = createResponse(fbReferenceUrl, HttpURLConnection.HTTP_OK, gson.toJson(ServerValues.TIMESTAMP));

    capturedCompletionHandler.getValue().onCompleted(response);
  }

  @Test
  public void testUpdateValue_forbidden() throws Exception {
    expectUpdateRequest(null);

    executedForbiddenRequestTest(ref.updateValue((SampleData) null));
  }

  @Test
  public void testUpdateValue_unauthorized() throws Exception {
    expectUpdateRequest(null);

    executedUnauthorizedRequestTest(ref.updateValue((SampleData) null));
  }

  @Test
  public void testUpdateValue_unsupportedStatusCode() throws Exception {
    expectUpdateRequest(sampleData);
    executedRequestWithUnsupportedResponseTest(ref.updateValue(sampleData), HttpURLConnection.HTTP_GATEWAY_TIMEOUT);

    expectUpdateRequest(sampleData);
    executedRequestWithUnsupportedResponseTest(ref.updateValue(sampleData), HttpURLConnection.HTTP_INTERNAL_ERROR);

    expectUpdateRequest(sampleData);
    executedRequestWithUnsupportedResponseTest(ref.updateValue(sampleData), HttpURLConnection.HTTP_NOT_FOUND);
  }

  @Test
  public void testUpdateValue_success() throws Exception {
    expectUpdateRequest(sampleData);

    Promise<SampleData, FirebaseRuntimeException, Void> result = ref.updateValue(sampleData);

    result.then(new DoneCallback<SampleData>() {
      @Override
      public void onDone(SampleData result) {
        assertSame(sampleData, result);
      }
    }).fail(new FailCallback<FirebaseRuntimeException>() {
      @Override
      public void onFail(FirebaseRuntimeException result) {
        fail("The promise should not have been rejected");
      }
    });

    Response response = createResponse(fbReferenceUrl, HttpURLConnection.HTTP_OK, gson.toJson(sampleData));

    capturedCompletionHandler.getValue().onCompleted(response);
  }

  @Test
  public void testRemoveValue_forbidden() throws Exception {
    expectRemoveRequest();

    executedForbiddenRequestTest(ref.removeValue());
  }

  @Test
  public void testRemoveValue_unauthorized() throws Exception {
    expectRemoveRequest();

    executedUnauthorizedRequestTest(ref.removeValue());
  }

  @Test
  public void testRemoveValue_unsupportedStatusCode() throws Exception {
    expectRemoveRequest();
    executedRequestWithUnsupportedResponseTest(ref.removeValue(), HttpURLConnection.HTTP_GATEWAY_TIMEOUT);

    expectRemoveRequest();
    executedRequestWithUnsupportedResponseTest(ref.removeValue(), HttpURLConnection.HTTP_INTERNAL_ERROR);

    expectRemoveRequest();
    executedRequestWithUnsupportedResponseTest(ref.removeValue(), HttpURLConnection.HTTP_NOT_FOUND);
  }

  @Test
  public void testRemoveValue_success() throws Exception {
    expectRemoveRequest();

    Promise<Void, FirebaseRuntimeException, Void> result = ref.removeValue();

    result.then(new DoneCallback<Void>() {
      @Override
      public void onDone(Void result) {
        assertNull(result);
      }
    }).fail(new FailCallback<FirebaseRuntimeException>() {
      @Override
      public void onFail(FirebaseRuntimeException result) {
        fail("The promise should not have been rejected");
      }
    });

    Response response = createResponse(fbReferenceUrl, HttpURLConnection.HTTP_OK, gson.toJson(sampleData));

    capturedCompletionHandler.getValue().onCompleted(response);
  }

  @Test
  public void testPush_forbidden() throws Exception {
    expectPushRequest();

    executedForbiddenRequestTest(ref.push());
  }

  @Test
  public void testPush_unauthorized() throws Exception {
    expectPushRequest();

    executedUnauthorizedRequestTest(ref.push());
  }

  @Test
  public void testPush_unsupportedStatusCode() throws Exception {
    expectPushRequest();
    executedRequestWithUnsupportedResponseTest(ref.push(), HttpURLConnection.HTTP_GATEWAY_TIMEOUT);

    expectPushRequest();
    executedRequestWithUnsupportedResponseTest(ref.push(), HttpURLConnection.HTTP_INTERNAL_ERROR);

    expectPushRequest();
    executedRequestWithUnsupportedResponseTest(ref.push(), HttpURLConnection.HTTP_NOT_FOUND);
  }

  @Test
  public void testPush_success() throws Exception {
    final PushResponse pushResponse = new PushResponse("abc");

    expectPushRequest();

    Promise<FirebaseRestReference, FirebaseRuntimeException, Void> result = ref.push();

    result.then(new DoneCallback<FirebaseRestReference>() {
      @Override
      public void onDone(FirebaseRestReference result) {
        String expectedReferenceUrl = fbReferenceUrl + PathUtil.FORWARD_SLASH + pushResponse.getName();
        assertEquals(expectedReferenceUrl, result.getReferenceUrl());
      }
    }).fail(new FailCallback<FirebaseRuntimeException>() {
      @Override
      public void onFail(FirebaseRuntimeException result) {
        fail("The promise should not have been rejected");
      }
    });

    Response response = createResponse(fbReferenceUrl, HttpURLConnection.HTTP_OK, gson.toJson(pushResponse));

    capturedCompletionHandler.getValue().onCompleted(response);
  }

  @Test
  public void testGetPriority_forbidden() throws Exception {
    expectGetPriorityRequest();
    executedForbiddenRequestTest(ref.getPriority());
  }

  @Test
  public void testGetPriority_unauthorized() throws Exception {
    expectGetPriorityRequest();
    executedUnauthorizedRequestTest(ref.getPriority());
  }

  @Test
  public void testGetPriority_unsupportedStatusCode() throws Exception {
    expectGetPriorityRequest();
    executedRequestWithUnsupportedResponseTest(ref.getPriority(), HttpURLConnection.HTTP_GATEWAY_TIMEOUT);

    expectGetPriorityRequest();
    executedRequestWithUnsupportedResponseTest(ref.getPriority(), HttpURLConnection.HTTP_INTERNAL_ERROR);

    expectGetPriorityRequest();
    executedRequestWithUnsupportedResponseTest(ref.getPriority(), HttpURLConnection.HTTP_NOT_FOUND);
  }

  @Test
  public void testGetPriority_success() throws Exception {
    final Double expectedPriority = 2.4;

    expectGetPriorityRequest();

    Promise<Double, FirebaseRuntimeException, Void> result = ref.getPriority();

    result.then(new DoneCallback<Double>() {
      @Override
      public void onDone(Double result) {
        assertEquals(expectedPriority, result);
      }
    }).fail(new FailCallback<FirebaseRuntimeException>() {
      @Override
      public void onFail(FirebaseRuntimeException result) {
        fail("The promise should not have been rejected");
      }
    });

    Response response = createResponse(fbReferenceUrl, HttpURLConnection.HTTP_OK, gson.toJson(expectedPriority));

    capturedCompletionHandler.getValue().onCompleted(response);
  }

  @Test
  public void testSetPriority_forbidden() throws Exception {
    final Double priority = 1.1;
    expectSetPriorityRequest(priority);

    executedForbiddenRequestTest(ref.setPriority(priority));
  }

  @Test
  public void testSetPriority_unauthorized() throws Exception {
    final Double priority = 1.1;
    expectSetPriorityRequest(priority);

    executedUnauthorizedRequestTest(ref.setPriority(priority));
  }

  @Test
  public void testSetPriority_unsupportedStatusCode() throws Exception {
    final Double priority = 1.1;

    expectSetPriorityRequest(priority);
    executedRequestWithUnsupportedResponseTest(ref.setPriority(priority), HttpURLConnection.HTTP_GATEWAY_TIMEOUT);

    expectSetPriorityRequest(priority);
    executedRequestWithUnsupportedResponseTest(ref.setPriority(priority), HttpURLConnection.HTTP_INTERNAL_ERROR);

    expectSetPriorityRequest(priority);
    executedRequestWithUnsupportedResponseTest(ref.setPriority(priority), HttpURLConnection.HTTP_NOT_FOUND);
  }

  @Test
  public void testSetPriority_success() throws Exception {
    final Double priority = 1.3;

    expectSetPriorityRequest(priority);

    Promise<Void, FirebaseRuntimeException, Void> result = ref.setPriority(priority);

    result.then(new DoneCallback<Void>() {
      @Override
      public void onDone(Void result) {
        assertNull(result);
      }
    }).fail(new FailCallback<FirebaseRuntimeException>() {
      @Override
      public void onFail(FirebaseRuntimeException result) {
        fail("The promise should not have been rejected");
      }
    });

    Response response = createResponse(fbReferenceUrl, HttpURLConnection.HTTP_OK, gson.toJson(priority));

    capturedCompletionHandler.getValue().onCompleted(response);
  }

  private void executeSuccessfulGetValueRequest(FirebaseRestReferenceImpl restReference) throws Exception {
    final SampleData expectedSampleData = new SampleData("aValue", 123);

    expectGetRequest();

    Promise<SampleData, FirebaseRuntimeException, Void> result = restReference.getValue(SampleData.class);

    result.then(new DoneCallback<SampleData>() {
      @Override
      public void onDone(SampleData result) {
        assertEquals(expectedSampleData, result);
      }
    }).fail(new FailCallback<FirebaseRuntimeException>() {
      @Override
      public void onFail(FirebaseRuntimeException result) {
        fail("The promise should not have been rejected");
      }
    });

    Response response = createResponse(fbReferenceUrl, HttpURLConnection.HTTP_OK, gson.toJson(expectedSampleData));

    capturedCompletionHandler.getValue().onCompleted(response);
  }

  private <TResult> void executedRequestWithUnsupportedResponseTest(Promise<TResult, FirebaseRuntimeException, Void> result, int responseCode) throws Exception {
    executedFailedRequestTest(result, responseCode, FirebaseRestException.class, null, FirebaseRuntimeException.ErrorCode.UnsupportedStatusCode);
  }

  private <TResult> void executedForbiddenRequestTest(Promise<TResult, FirebaseRuntimeException, Void> result) throws Exception {
    executedFailedRequestTest(result, HttpURLConnection.HTTP_FORBIDDEN, FirebaseAccessException.class, null, FirebaseRuntimeException.ErrorCode.AccessViolation);
  }

  private <TResult> void executedUnauthorizedRequestTest(Promise<TResult, FirebaseRuntimeException, Void> result) throws Exception {
    executedFailedRequestTest(result, HttpURLConnection.HTTP_UNAUTHORIZED, FirebaseAccessException.class, null, FirebaseRuntimeException.ErrorCode.AccessViolation);
  }

  private <TResult, TException extends FirebaseRuntimeException> void executedFailedRequestTest(Promise<TResult, FirebaseRuntimeException, Void> result, int statusCode, final Class<TException> exceptionClazz, String requestBody, final FirebaseRuntimeException.ErrorCode expectedErrorCode) throws Exception {
    result.then(new DoneCallback<TResult>() {
      @Override
      public void onDone(TResult result) {
        fail("The promise should not have been resolved");
      }
    }).fail(new FailCallback<FirebaseRuntimeException>() {
      @Override
      public void onFail(FirebaseRuntimeException result) {
        assertEquals(exceptionClazz, result.getClass());
        assertEquals(expectedErrorCode, result.getErrorCode());
      }
    });

    Response response = createResponse(fbReferenceUrl, statusCode, requestBody);

    capturedCompletionHandler.getValue().onCompleted(response);

    assertIsSatisfied();
  }

  private void expectPushRequest() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).preparePost(getFirebaseRestUrl()); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setBody("{}"); will(returnValue(requestBuilder));
      oneOf(requestBuilder).execute(with(aNonNull(AsyncCompletionHandler.class))); will(MockObjectHelper.capture(capturedCompletionHandler));
    }});
  }

  private void expectRemoveRequest() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).prepareDelete(getFirebaseRestUrl()); will(returnValue(requestBuilder));
      oneOf(requestBuilder).execute(with(aNonNull(AsyncCompletionHandler.class))); will(MockObjectHelper.capture(capturedCompletionHandler));
    }});
  }

  private <T> void expectUpdateRequest(final T data) {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).preparePatch(getFirebaseRestUrl()); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setBody(gson.toJson(data)); will(returnValue(requestBuilder));
      oneOf(requestBuilder).execute(with(aNonNull(AsyncCompletionHandler.class))); will(MockObjectHelper.capture(capturedCompletionHandler));
    }});
  }

  private <T> void expectSetRequest(final T data) {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).preparePut(getFirebaseRestUrl()); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setBody(gson.toJson(data)); will(returnValue(requestBuilder));
      oneOf(requestBuilder).execute(with(aNonNull(AsyncCompletionHandler.class))); will(MockObjectHelper.capture(capturedCompletionHandler));
    }});
  }

  private <T> void expectSetPriorityRequest(final Double priority) {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).preparePut(getPriorityRestUrl()); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setBody(gson.toJson(priority)); will(returnValue(requestBuilder));
      oneOf(requestBuilder).execute(with(aNonNull(AsyncCompletionHandler.class))); will(MockObjectHelper.capture(capturedCompletionHandler));
    }});
  }

  private void expectGetRequest() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).prepareGet(getFirebaseRestUrl()); will(returnValue(requestBuilder));
      oneOf(requestBuilder).execute(with(aNonNull(AsyncCompletionHandler.class))); will(MockObjectHelper.capture(capturedCompletionHandler));
    }});
  }

  private void expectGetPriorityRequest() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).prepareGet(getPriorityRestUrl()); will(returnValue(requestBuilder));
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

  private String getPriorityRestUrl() {
    return fbReferenceUrl + "/.priority" + FirebaseDocumentLocation.JSON_SUFFIX;
  }

  private String getFirebaseRestUrl() {
    return fbReferenceUrl + FirebaseDocumentLocation.JSON_SUFFIX;
  }
}
