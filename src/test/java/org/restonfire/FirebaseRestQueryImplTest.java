package org.restonfire;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Param;
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
import org.restonfire.testdata.SampleData;
import org.restonfire.testutils.AbstractMockTestCase;
import org.restonfire.testutils.MockObjectHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests for FirebaseRestQueryImpl.
 */
public class FirebaseRestQueryImplTest extends AbstractMockTestCase {

  private final AsyncHttpClient.BoundRequestBuilder requestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);

  private final Gson gson = new GsonBuilder().create();
  private final MutableObject<AsyncCompletionHandler<Void>> capturedCompletionHandler = new MutableObject<>();

  private final String referenceUrl = "https://mynamespace.firebaseio.com/some/path";
  private final SampleData sampleData = new SampleData("foobar", 123);

  private final FirebaseRestQueryImpl query = new FirebaseRestQueryImpl(gson, requestBuilder, referenceUrl);

  @Test
  public void testStartAt_StringValue() {
    String someVal = "abc";

    query.startAt(someVal);

    expectRequestexecution(new Param("startAt", gson.toJson(someVal)));

    query.run(String.class);
  }

  @Test
  public void testStartAt_BooleanValue() {
    query.startAt(true);

    expectRequestexecution(new Param("startAt", gson.toJson(true)));

    query.run(Boolean.class);
  }

  @Test
  public void testEndAt_StringValue() {
    String someVal = "abc";

    query.endAt(someVal);

    expectRequestexecution(new Param("endAt", gson.toJson(someVal)));

    query.run(String.class);
  }

  @Test
  public void testEndAt_BooleanValue() {
    query.endAt(true);

    expectRequestexecution(new Param("endAt", gson.toJson(true)));

    query.run(Boolean.class);
  }

  @Test
  public void testEqualTo_StringValue() {
    String someVal = "abc";

    query.equalTo(someVal);

    expectRequestexecution(new Param("equalTo", gson.toJson(someVal)));

    query.run(String.class);
  }

  @Test
  public void testEqualTo_BooleanValue() {
    query.equalTo(true);

    expectRequestexecution(new Param("equalTo", gson.toJson(true)));

    query.run(Boolean.class);
  }

  @Test
  public void testLimitToFirst() {
    query.limitToFirst(3);

    expectRequestexecution(new Param("limitToFirst", gson.toJson(3)));

    query.run(String.class);
  }

  @Test
  public void testLimitToLast() {
    query.limitToLast(5);

    expectRequestexecution(new Param("limitToLast", gson.toJson(5)));

    query.run(String.class);
  }

  @Test
  public void testOrderByChild() {
    final String childName = "foo";

    query.orderByChild(childName);

    expectRequestexecution(new Param("orderBy", gson.toJson(childName)));

    query.run(String.class);
  }

  @Test
  public void testOrderByKey() {
    query.orderByKey();

    expectRequestexecution(new Param("orderBy", gson.toJson("$key")));

    query.run(String.class);
  }

  @Test
  public void testOrderByPriority() {
    query.orderByPriority();

    expectRequestexecution(new Param("orderBy", gson.toJson("$priority")));

    query.run(String.class);
  }

  @Test
  public void testOrderByValue() {
    query.orderByValue();

    expectRequestexecution(new Param("orderBy", gson.toJson("$value")));

    query.run(String.class);
  }

  @Test
  public void testFullQuery() {
    query
      .orderByValue()
      .startAt("bar")
      .endAt("foo")
      .limitToFirst(10);

    expectRequestexecution(
      new Param("orderBy", gson.toJson("$value")),
      new Param("limitToFirst", gson.toJson(10)),
      new Param("startAt", gson.toJson("bar")),
      new Param("endAt", gson.toJson("foo"))
    );

    query.run(String.class);
  }

  @Test
  public void testRunQuery_success() throws Exception {
    final SampleData expectedSampleData = new SampleData("aValue", 123);

    expectGetRequest();

    Promise<SampleData, FirebaseRuntimeException, Void> result = query.run(SampleData.class);

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

    Response response = createResponse(referenceUrl, HttpURLConnection.HTTP_OK, gson.toJson(expectedSampleData));

    capturedCompletionHandler.getValue().onCompleted(response);
  }

  @Test
  public void testRunQuery_forbidden() throws Exception {
    expectGetRequest();
    executedForbiddenRequestTest(query.run(SampleData.class));
  }

  @Test
  public void testGetValue_unauthorized() throws Exception {
    expectGetRequest();
    executedUnauthorizedRequestTest(query.run(SampleData.class));
  }

  @Test
  public void testGetValue_unsupportedStatusCode() throws Exception {
    expectGetRequest();
    executedRequestWithUnsupportedResponseTest(query.run(SampleData.class), HttpURLConnection.HTTP_GATEWAY_TIMEOUT);

    expectGetRequest();
    executedRequestWithUnsupportedResponseTest(query.run(SampleData.class), HttpURLConnection.HTTP_INTERNAL_ERROR);

    expectGetRequest();
    executedRequestWithUnsupportedResponseTest(query.run(SampleData.class), HttpURLConnection.HTTP_NOT_FOUND);
  }

  @Test
  public void testClear() {
    // This will set the state of the query to contain multiple filters.
    testFullQuery();
    assertIsSatisfied();

    //Re-running the query should set the same filters
    expectRequestexecution(
      new Param("orderBy", gson.toJson("$value")),
      new Param("limitToFirst", gson.toJson(10)),
      new Param("startAt", gson.toJson("bar")),
      new Param("endAt", gson.toJson("foo"))
    );

    query.run(String.class);
    assertIsSatisfied();

    query.clear();

    //No, all filters have been removed.
    expectGetRequest();
    query.run(String.class);
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

    Response response = createResponse(referenceUrl, statusCode, requestBody);

    capturedCompletionHandler.getValue().onCompleted(response);

    assertIsSatisfied();
  }

  private void expectGetRequest() {
    addExpectations(new Expectations() {{
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

  private void expectRequestexecution(Param... params) {
    expectParams(params);

    addExpectations(new Expectations() {{
      oneOf(requestBuilder).execute(with(any(AsyncCompletionHandler.class))); will(MockObjectHelper.capture(capturedCompletionHandler));
    }});
  }

  private void expectParams(final Param[] params) {
    addExpectations(new Expectations() {{
      oneOf(requestBuilder).addQueryParams(with(MockObjectHelper.elementsAreEqual(Arrays.asList(params))));
    }});
  }
}
