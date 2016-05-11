package org.restonfire;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ning.http.client.*;
import com.ning.http.client.providers.jdk.ResponseBodyPart;
import org.apache.commons.lang3.mutable.MutableObject;
import org.hamcrest.beans.HasPropertyWithValue;
import org.hamcrest.core.Is;
import org.jdeferred.*;
import org.jmock.Expectations;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.restonfire.exceptions.*;
import org.restonfire.fakes.FakeResponseHeaders;
import org.restonfire.fakes.FakeResponseStatus;
import org.restonfire.responses.EventStreamResponse;
import org.restonfire.testdata.SampleData;
import org.restonfire.testutils.AbstractMockTestCase;
import org.restonfire.testutils.MockObjectHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link FirebaseRestEventStreamImpl} class.
 */
public class FirebaseRestEventStreamImplTest extends AbstractMockTestCase {

  private final AsyncHttpClient asyncHttpClient = mock(AsyncHttpClient.class);
  private final AsyncHttpClient.BoundRequestBuilder requestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);
  private final ListenableFuture<Void> listenableFuture = mock(ListenableFuture.class);

  private final Gson gson = new GsonBuilder().create();
  private final String fbBaseUrl = "https://mynamespace.firebaseio.com";
  private final String path = "foo/bar";
  private final String fbReferenceUrl = fbBaseUrl + PathUtil.FORWARD_SLASH + path;
  private final SampleData sampleData = new SampleData("foobar", 123);

  private final MutableObject<AsyncHandler<Void>> capturedRequestHandler = new MutableObject<>();

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testGetReferenceUrl() {
    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH + path, createEventStream().getReferenceUrl());
  }

  @Test
  public void testGetRoot() {
    FirebaseRestEventStreamImpl eventStream = createEventStream();

    expectEventStreamCreation(fbBaseUrl + PathUtil.FORWARD_SLASH + FirebaseDocumentLocation.JSON_SUFFIX);
    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH, eventStream.getRoot().getReferenceUrl());
  }

  @Test
  public void testGetParent() {
    FirebaseRestEventStreamImpl eventStream = createEventStream();

    expectEventStreamCreation(fbBaseUrl + PathUtil.FORWARD_SLASH + "foo" + FirebaseDocumentLocation.JSON_SUFFIX);
    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH + "foo", eventStream.getParent().getReferenceUrl());
  }

  @Test
  public void testChild() {
    FirebaseRestEventStreamImpl eventStream = createEventStream();

    expectEventStreamCreation(fbReferenceUrl + "/test" + FirebaseDocumentLocation.JSON_SUFFIX);
    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH + path + PathUtil.FORWARD_SLASH + "test", eventStream.child("test").getReferenceUrl());
    assertIsSatisfied();

    expectEventStreamCreation(fbReferenceUrl + "/test/something" + FirebaseDocumentLocation.JSON_SUFFIX);
    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH + path + PathUtil.FORWARD_SLASH + "test/something", eventStream.child("test/something/").getReferenceUrl());
  }

  @Test
  public void testStartListening_headerReceived() throws Exception {
    FirebaseRestEventStreamImpl eventStream = createEventStream();

    expectListenerStart();
    Promise<Void, FirebaseRuntimeException, EventStreamResponse> result = eventStream.startListening();

    expectPromiseUntouched(result);
    capturedRequestHandler.getValue().onHeadersReceived(new FakeResponseHeaders());
  }

  @Test
  public void testStartListening_forbidden() throws Exception {
    FirebaseRestEventStreamImpl eventStream = createEventStream();

    expectListenerStart();
    Promise<Void, FirebaseRuntimeException, EventStreamResponse> result = eventStream.startListening();

    executedFailedRequestTest(result, HttpURLConnection.HTTP_FORBIDDEN, FirebaseAccessException.class, FirebaseRuntimeException.ErrorCode.AccessViolation);
  }

  @Test
  public void testStartListening_unauthorized() throws Exception {
    FirebaseRestEventStreamImpl eventStream = createEventStream();

    expectListenerStart();
    Promise<Void, FirebaseRuntimeException, EventStreamResponse> result = eventStream.startListening();

    executedFailedRequestTest(result, HttpURLConnection.HTTP_UNAUTHORIZED, FirebaseAccessException.class, FirebaseRuntimeException.ErrorCode.AccessViolation);
  }

  @Test
  public void testStartListening_unsupportedStatusCode() throws Exception {
    FirebaseRestEventStreamImpl eventStream = createEventStream();

    expectListenerStart();
    Promise<Void, FirebaseRuntimeException, EventStreamResponse> result = eventStream.startListening();

    executedFailedRequestTest(result, HttpURLConnection.HTTP_GATEWAY_TIMEOUT, FirebaseRestException.class, FirebaseRuntimeException.ErrorCode.UnsupportedStatusCode);
  }

  @Test
  public void testStartListening_requestDisconnected() throws Exception {
    FirebaseRestEventStreamImpl eventStream = createEventStream();

    expectListenerStart();
    Promise<Void, FirebaseRuntimeException, EventStreamResponse> result = eventStream.startListening();

    expectPromiseRejection(result, FirebaseRestException.class, FirebaseRuntimeException.ErrorCode.EventStreamRequestFailed);

    capturedRequestHandler.getValue().onThrowable(new TimeoutException());
  }

  @Test
  public void testStartListening_connectionFailed() throws Exception {
    FirebaseRestEventStreamImpl eventStream = createEventStream();

    expectListenerStart();
    Promise<Void, FirebaseRuntimeException, EventStreamResponse> result = eventStream.startListening();

    expectPromiseRejection(result, FirebaseRestException.class, FirebaseRuntimeException.ErrorCode.EventStreamRequestFailed);

    capturedRequestHandler.getValue().onThrowable(new UnknownHostException("myfirebase.firebaseio.com"));
  }

  @Test
  public void testStartListening_streamAccessCancelled() throws Exception {
    FirebaseRestEventStreamImpl eventStream = createEventStream();

    expectListenerStart();
    Promise<Void, FirebaseRuntimeException, EventStreamResponse> result = eventStream.startListening();

    expectPromiseRejection(result, FirebaseAccessException.class, FirebaseRuntimeException.ErrorCode.AccessViolation);

    sendEvent("cancel", "null", true);
  }

  @Test
  public void testStartListening_accessTokenExpired() throws Exception {
    FirebaseRestEventStreamImpl eventStream = createEventStream();

    expectListenerStart();
    Promise<Void, FirebaseRuntimeException, EventStreamResponse> result = eventStream.startListening();

    expectPromiseRejection(result, FirebaseAuthenticationExpiredException.class, FirebaseRuntimeException.ErrorCode.AuthenticationExpired);

    sendEvent("auth_revoked", "The token timed out", true);
  }

  @Test
  public void testStartListening_keepAliveReceived() throws Exception {
    FirebaseRestEventStreamImpl eventStream = createEventStream();

    expectListenerStart();
    Promise<Void, FirebaseRuntimeException, EventStreamResponse> result = eventStream.startListening();

    expectPromiseUntouched(result);

    sendEvent("keep-alive", "null", false);
    sendEvent("keep-alive", "null", false);
    sendEvent("keep-alive", "null", false);
  }

  @Test
  public void testStartListening_eventReceived() throws Exception {
    executeSuccessfulEventRetrievalTest(createEventStream());
  }

  @Test
  public void testStartListening_listeningAlreadyStarted() throws Exception {
    FirebaseRestEventStreamImpl eventStream = createEventStream();

    executeSuccessfulEventRetrievalTest(eventStream);

    exception.expect(FirebaseInvalidStateException.class);
    exception.expect(HasPropertyWithValue.hasProperty("errorCode", Is.is(FirebaseRuntimeException.ErrorCode.EventStreamListenerAlreadyActive)));

    eventStream.startListening();
  }

  @Test
  public void testStopListening_eventStreamNotActive() {
    exception.expect(FirebaseInvalidStateException.class);
    exception.expect(HasPropertyWithValue.hasProperty("errorCode", Is.is(FirebaseRuntimeException.ErrorCode.EventStreamListenerNotActive)));

    createEventStream().stopListening();
  }

  @Test
  public void testStopListening_success() throws Exception {
    FirebaseRestEventStreamImpl eventStream = createEventStream();

    Promise<Void, FirebaseRuntimeException, EventStreamResponse> result = executeSuccessfulEventRetrievalTest(eventStream);

    result.always(new AlwaysCallback<Void, FirebaseRuntimeException>() {
      @Override
      public void onAlways(Promise.State state, Void resolved, FirebaseRuntimeException rejected) {
        assertEquals(Promise.State.RESOLVED, state);
        assertNull(resolved);
        assertNull(rejected);
      }
    });

    addExpectations(new Expectations() {{
      oneOf(listenableFuture).done();
    }});
    eventStream.stopListening();

    capturedRequestHandler.getValue().onCompleted();
  }

  private void expectPromiseUntouched(Promise<Void, FirebaseRuntimeException, EventStreamResponse> result) {
    result
      .always(new AlwaysCallback<Void, FirebaseRuntimeException>() {
        @Override
        public void onAlways(Promise.State state, Void resolved, FirebaseRuntimeException rejected) {
          fail("Promise should not have been rejected or resolved.");
        }
      })
      .progress(new ProgressCallback<EventStreamResponse>() {
        @Override
        public void onProgress(EventStreamResponse progress) {
          fail("No progress should have been reported.");
        }
      });
  }

  private Promise<Void, FirebaseRuntimeException, EventStreamResponse> executeSuccessfulEventRetrievalTest(FirebaseRestEventStreamImpl eventStream) throws Exception {
    expectListenerStart();
    Promise<Void, FirebaseRuntimeException, EventStreamResponse> result = eventStream.startListening();

    final MutableObject expectedValue = new MutableObject(1);
    result
      .progress(new ProgressCallback<EventStreamResponse>() {
        @Override
        public void onProgress(EventStreamResponse progress) {
          assertEquals(expectedValue.getValue().toString(), progress.getSerialzedEventData());
          assertEquals(expectedValue.getValue(), progress.getEventData(expectedValue.getValue().getClass()));
        }
      });

    sendEvent("put", "1", false);

    expectedValue.setValue(3);
    sendEvent("put", "3", false);

    expectedValue.setValue(true);
    sendEvent("patch", "true", false);

    return result;
  }

  private void sendEvent(String eventType, String data, boolean isLastEvent) throws Exception {
    assertEquals(
      AsyncHandler.STATE.CONTINUE,
      capturedRequestHandler.getValue().onBodyPartReceived(createBodyPart(eventType, data, isLastEvent))
    );
  }

  private HttpResponseBodyPart createBodyPart(String eventType, String data, boolean isLastPart) {
    return new ResponseBodyPart(
      String.format("event: %s\ndata: %s", eventType, data).getBytes(Charset.forName("UTF-8")),
      isLastPart);
  }

  private <TException extends FirebaseRuntimeException> void executedFailedRequestTest(
    Promise<Void, FirebaseRuntimeException, EventStreamResponse> result,
    int statusCode,
    final Class<TException> exoectedExceptionClazz,
    final FirebaseRuntimeException.ErrorCode expectedErrorCode) throws Exception {

    expectPromiseRejection(result, exoectedExceptionClazz, expectedErrorCode);

    HttpResponseStatus responseStatus = createResponseStatus(fbReferenceUrl, statusCode);
    capturedRequestHandler.getValue().onStatusReceived(responseStatus);

    assertIsSatisfied();
  }

  private <TException extends FirebaseRuntimeException> void expectPromiseRejection(
    Promise<Void, FirebaseRuntimeException, EventStreamResponse> result,
    final Class<TException> exoectedExceptionClazz,
    final FirebaseRuntimeException.ErrorCode expectedErrorCode) {

    result
      .then(new DoneCallback<Void>() {
        @Override
        public void onDone(Void result) {
          fail("The promise should not have been resolved");
        }
      })
      .progress(new ProgressCallback<EventStreamResponse>() {
        @Override
        public void onProgress(EventStreamResponse progress) {
          fail("The promise should not have been resolved");
        }
      })
      .fail(new FailCallback<FirebaseRuntimeException>() {
        @Override
        public void onFail(FirebaseRuntimeException result) {
          assertEquals(exoectedExceptionClazz, result.getClass());
          assertEquals(expectedErrorCode, result.getErrorCode());
        }
      });
  }

  private FirebaseRestEventStreamImpl createEventStream() {
    expectEventStreamCreation(getFirebaseRestUrl());

    FirebaseRestEventStreamImpl result = new FirebaseRestEventStreamImpl(
      asyncHttpClient,
      gson,
      fbBaseUrl,
      null,
      path
    );

    assertIsSatisfied();

    return result;
  }

  private void expectEventStreamCreation(final String referenceUrl) {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).prepareGet(referenceUrl); will(returnValue(requestBuilder));
      oneOf(requestBuilder).addHeader("Accept", "text/event-stream"); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setFollowRedirects(true); will(returnValue(requestBuilder));
    }});
  }

  private void expectListenerStart() {
    addExpectations(new Expectations() {{
      oneOf(requestBuilder).execute(with(aNonNull(AsyncHandler.class))); will(doAll(MockObjectHelper.capture(capturedRequestHandler), returnValue(listenableFuture)));
    }});
  }

  private HttpResponseStatus createResponseStatus(final String url, final int statusCode) throws IOException {
    return new FakeResponseStatus(url, statusCode);
  }

  private String getFirebaseRestUrl() {
    return fbReferenceUrl + FirebaseDocumentLocation.JSON_SUFFIX;
  }
}
