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
import org.restonfire.testutils.AbstractMockTestCase;
import org.restonfire.testutils.MockObjectHelper;
import org.restonfire.utils.PathUtil;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Test class for FirebaseRestReferenceImpl.
 *
 * Created by jfischer on 2016-04-20.
 */
public class FirebaseRestReferenceImplTest extends AbstractMockTestCase {

  private final AsyncHttpClient asyncHttpClient = mock(AsyncHttpClient.class);
  private final AsyncHttpClient.BoundRequestBuilder requestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);

  private final Gson gson = new GsonBuilder().create();
  private final String fbBaseUrl = "https://mynamespace.firebaseio.com";
  private final String fbAccessToken = "fbAccessToken";
  private final String path = "foo/par";
  private final String fbReferenceUrl = fbBaseUrl + PathUtil.FORWARD_SLASH + path;
  private final SampleData sampleData = new SampleData("foobar", 123);

  private final MutableObject<AsyncCompletionHandler<Void>> capturedCompletionHandler = new MutableObject<>();

  private final FirebaseRestReferenceImpl ref = new FirebaseRestReferenceImpl(
    asyncHttpClient,
    gson,
    fbBaseUrl,
    fbAccessToken,
    path
  );

  @Test
  public void testGetReferenceUrl() {
    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH + path, ref.getReferenceUrl());
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
    final SampleData expectedSampleData = new SampleData("aValue", 123);

    expectGetRequest();

    Promise<SampleData, FirebaseRuntimeException, Void> result = ref.getValue(SampleData.class);

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

  private <T extends FirebaseRuntimeException> void executedFailedRequestTest(Promise<SampleData, FirebaseRuntimeException, Void> result, int statusCode, final Class<T> exceptionClazz, String requestBody) throws Exception {
    result.then(new DoneCallback<SampleData>() {
      @Override
      public void onDone(SampleData result) {
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
