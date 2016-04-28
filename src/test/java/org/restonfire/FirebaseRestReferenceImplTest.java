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
import org.restonfire.testutils.AbstractMockTestCase;
import org.restonfire.testutils.MockObjectHelper;
import org.restonfire.utils.PathUtil;

import java.io.IOException;
import java.net.HttpURLConnection;

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
    executedFailedRequestTest(ref.getValue(SampleData.class), HttpURLConnection.HTTP_FORBIDDEN, FirebaseAccessException.class, null);
  }

  @Test
  public void testGetValue_unsupportedStatusCode() throws Exception {
    expectGetRequest();
    executedFailedRequestTest(ref.getValue(SampleData.class), HttpURLConnection.HTTP_GATEWAY_TIMEOUT, FirebaseRestException.class, null);

    expectGetRequest();
    executedFailedRequestTest(ref.getValue(SampleData.class), HttpURLConnection.HTTP_INTERNAL_ERROR, FirebaseRestException.class, null);

    expectGetRequest();
    executedFailedRequestTest(ref.getValue(SampleData.class), HttpURLConnection.HTTP_NOT_FOUND, FirebaseRestException.class, null);
  }

  @Test
  public void testGetValue_invalidClassType() throws Exception {
    expectGetRequest();
    executedFailedRequestTest(ref.getValue(SampleData.class), HttpURLConnection.HTTP_OK, FirebaseRestException.class, "{aString: 'abc', anInt: 'foo'}");
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

    executedFailedRequestTest(ref.setValue((SampleData) null), HttpURLConnection.HTTP_FORBIDDEN, FirebaseAccessException.class, null);
  }

  @Test
  public void testSetValue_unsupportedStatusCode() throws Exception {
    expectSetRequest(sampleData);
    executedFailedRequestTest(ref.setValue(sampleData), HttpURLConnection.HTTP_GATEWAY_TIMEOUT, FirebaseRestException.class, null);

    expectSetRequest(sampleData);
    executedFailedRequestTest(ref.setValue(sampleData), HttpURLConnection.HTTP_INTERNAL_ERROR, FirebaseRestException.class, null);

    expectSetRequest(sampleData);
    executedFailedRequestTest(ref.setValue(sampleData), HttpURLConnection.HTTP_NOT_FOUND, FirebaseRestException.class, null);
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
  public void testUpdateValue_forbidden() throws Exception {
    expectUpdateRequest(null);

    executedFailedRequestTest(ref.updateValue((SampleData) null), HttpURLConnection.HTTP_FORBIDDEN, FirebaseAccessException.class, null);
  }

  @Test
  public void testUpdateValue_unsupportedStatusCode() throws Exception {
    expectUpdateRequest(sampleData);
    executedFailedRequestTest(ref.updateValue(sampleData), HttpURLConnection.HTTP_GATEWAY_TIMEOUT, FirebaseRestException.class, null);

    expectUpdateRequest(sampleData);
    executedFailedRequestTest(ref.updateValue(sampleData), HttpURLConnection.HTTP_INTERNAL_ERROR, FirebaseRestException.class, null);

    expectUpdateRequest(sampleData);
    executedFailedRequestTest(ref.updateValue(sampleData), HttpURLConnection.HTTP_NOT_FOUND, FirebaseRestException.class, null);
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

    executedFailedRequestTest(ref.removeValue(), HttpURLConnection.HTTP_FORBIDDEN, FirebaseAccessException.class, null);
  }

  @Test
  public void testRemoveValue_unsupportedStatusCode() throws Exception {
    expectRemoveRequest();
    executedFailedRequestTest(ref.removeValue(), HttpURLConnection.HTTP_GATEWAY_TIMEOUT, FirebaseRestException.class, null);

    expectRemoveRequest();
    executedFailedRequestTest(ref.removeValue(), HttpURLConnection.HTTP_INTERNAL_ERROR, FirebaseRestException.class, null);

    expectRemoveRequest();
    executedFailedRequestTest(ref.removeValue(), HttpURLConnection.HTTP_NOT_FOUND, FirebaseRestException.class, null);
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

    executedFailedRequestTest(ref.push(), HttpURLConnection.HTTP_FORBIDDEN, FirebaseAccessException.class, null);
  }

  @Test
  public void testPush_unsupportedStatusCode() throws Exception {
    expectPushRequest();
    executedFailedRequestTest(ref.push(), HttpURLConnection.HTTP_GATEWAY_TIMEOUT, FirebaseRestException.class, null);

    expectPushRequest();
    executedFailedRequestTest(ref.push(), HttpURLConnection.HTTP_INTERNAL_ERROR, FirebaseRestException.class, null);

    expectPushRequest();
    executedFailedRequestTest(ref.push(), HttpURLConnection.HTTP_NOT_FOUND, FirebaseRestException.class, null);
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

  private <TResult, TException extends FirebaseRuntimeException> void executedFailedRequestTest(Promise<TResult, FirebaseRuntimeException, Void> result, int statusCode, final Class<TException> exceptionClazz, String requestBody) throws Exception {
    result.then(new DoneCallback<TResult>() {
      @Override
      public void onDone(TResult result) {
        fail("The promise should not have been resolved");
      }
    }).fail(new FailCallback<FirebaseRuntimeException>() {
      @Override
      public void onFail(FirebaseRuntimeException result) {
        assertEquals(exceptionClazz, result.getClass());
      }
    });

    Response response = createResponse(fbReferenceUrl, statusCode, requestBody);

    capturedCompletionHandler.getValue().onCompleted(response);

    assertIsSatisfied();
  }

  private void expectPushRequest() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).preparePost(fbReferenceUrl); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setBody("{}"); will(returnValue(requestBuilder));
      oneOf(requestBuilder).execute(with(aNonNull(AsyncCompletionHandler.class))); will(MockObjectHelper.capture(capturedCompletionHandler));
    }});
  }

  private void expectRemoveRequest() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).prepareDelete(fbReferenceUrl); will(returnValue(requestBuilder));
      oneOf(requestBuilder).execute(with(aNonNull(AsyncCompletionHandler.class))); will(MockObjectHelper.capture(capturedCompletionHandler));
    }});
  }

  private <T> void expectUpdateRequest(final T data) {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).preparePatch(fbReferenceUrl); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setBody(gson.toJson(data)); will(returnValue(requestBuilder));
      oneOf(requestBuilder).execute(with(aNonNull(AsyncCompletionHandler.class))); will(MockObjectHelper.capture(capturedCompletionHandler));
    }});
  }

  private <T> void expectSetRequest(final T data) {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).preparePut(fbReferenceUrl); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setBody(gson.toJson(data)); will(returnValue(requestBuilder));
      oneOf(requestBuilder).execute(with(aNonNull(AsyncCompletionHandler.class))); will(MockObjectHelper.capture(capturedCompletionHandler));
    }});
  }

  private void expectGetRequest() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).prepareGet(fbReferenceUrl); will(returnValue(requestBuilder));
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

  public static class SampleData {
    public String aString;
    public int anInt;

    public SampleData() {
      // do nothing
    }

    public SampleData(String aString, int anInt) {
      this.aString = aString;
      this.anInt = anInt;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      SampleData that = (SampleData) o;

      if (anInt != that.anInt) return false;
      return aString != null ? aString.equals(that.aString) : that.aString == null;
    }

    @Override
    public int hashCode() {
      int result = aString != null ? aString.hashCode() : 0;
      result = 31 * result + anInt;
      return result;
    }
  }
}
