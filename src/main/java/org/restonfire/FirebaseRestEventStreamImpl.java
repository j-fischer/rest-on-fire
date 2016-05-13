package org.restonfire;

import com.google.gson.Gson;
import com.ning.http.client.*;
import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.restonfire.exceptions.*;
import org.restonfire.responses.StreamingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>Non-thread safe</b> {@link FirebaseRestEventStream} implementation.
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "checkstyle:classdataabstractioncoupling", "checkstyle:classfanoutcomplexity"})
class FirebaseRestEventStreamImpl extends FirebaseDocumentLocation implements FirebaseRestEventStream {

  private static final Logger LOG = LoggerFactory.getLogger(FirebaseRestEventStreamImpl.class);

  private static final Map<String, StreamingEvent.EventType> EVENT_TYPE_MAPPER;
  static {
    EVENT_TYPE_MAPPER = new HashMap<>(5);
    EVENT_TYPE_MAPPER.put("put", StreamingEvent.EventType.Set);
    EVENT_TYPE_MAPPER.put("patch", StreamingEvent.EventType.Update);
    EVENT_TYPE_MAPPER.put("keep-alive", StreamingEvent.EventType.KeepAlive);
    EVENT_TYPE_MAPPER.put("cancel", StreamingEvent.EventType.Cancel);
    EVENT_TYPE_MAPPER.put("auth_revoked", StreamingEvent.EventType.Expired);
  }

  private final Gson gson;
  private final AsyncHttpClient asyncHttpClient;
  private final AsyncHttpClient.BoundRequestBuilder eventStreamRequest;

  private ListenableFuture<Void> currentListener;

  FirebaseRestEventStreamImpl(
    AsyncHttpClient asyncHttpClient,
    Gson gson,
    String fbBaseUrl,
    String fbAccessToken,
    String path) {

    super(fbBaseUrl, path, fbAccessToken);

    this.asyncHttpClient = asyncHttpClient;
    this.gson = gson;

    this.eventStreamRequest = RequestBuilderUtil.createGet(
        asyncHttpClient,
        referenceUrl,
        fbAccessToken
      ).addHeader("Accept", "text/event-stream")
       .setFollowRedirects(true);
  }

  @Override
  public Promise<Void, FirebaseRuntimeException, StreamingEvent> startListening() {
    LOG.debug("startListening() invoked for reference {}", referenceUrl);

    if (currentListener != null) {
      throw new FirebaseInvalidStateException(FirebaseRuntimeException.ErrorCode.EventStreamListenerAlreadyActive, "The EventStream is already running");
    }

    final Deferred<Void, FirebaseRuntimeException, StreamingEvent> deferred = new DeferredObject<>();
    final AsyncHandler<Void> asyncRequestHandler = createAsyncHandler(deferred);
    currentListener = eventStreamRequest.execute(asyncRequestHandler);

    return deferred.promise();
  }

  @Override
  public void stopListening() {
    if (currentListener == null) {
      throw new FirebaseInvalidStateException(FirebaseRuntimeException.ErrorCode.EventStreamListenerNotActive, "The EventStream is currently not active");
    }

    currentListener.done();
    currentListener = null;
  }

  @Override
  public FirebaseRestEventStream getRoot() {
    LOG.debug("getRoot() invoked for reference {}", referenceUrl);
    return new FirebaseRestEventStreamImpl(
      asyncHttpClient,
      gson,
      fbBaseUrl,
      fbAccessToken,
      ""
    );
  }

  @Override
  public FirebaseRestEventStream getParent() {
    LOG.debug("getParent() invoked for reference {}", referenceUrl);
    return new FirebaseRestEventStreamImpl(
      asyncHttpClient,
      gson,
      fbBaseUrl,
      fbAccessToken,
      PathUtil.getParent(path)
    );
  }

  @Override
  public FirebaseRestEventStream child(String childPath) {
    LOG.debug("child({}) invoked for reference {}", childPath, referenceUrl);
    return new FirebaseRestEventStreamImpl(
      asyncHttpClient,
      gson,
      fbBaseUrl,
      fbAccessToken,
      PathUtil.concatenatePath(path, childPath)
    );
  }

  @SuppressWarnings({"PMD.ExcessiveMethodLength", "checkstyle:anoninnerlength"})
  private AsyncHandler<Void> createAsyncHandler(final Deferred<Void, FirebaseRuntimeException, StreamingEvent> deferred) {
    return new AsyncHandler<Void>() {
      @Override
      public void onThrowable(Throwable t) {
        final String message = "EventStream request for location '" + referenceUrl + "' failed";
        LOG.error(message, t);
        deferred.reject(new FirebaseRestException(FirebaseRuntimeException.ErrorCode.EventStreamRequestFailed, message, t));
      }

      @Override
      public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        LOG.debug("Received event");

        final StreamingEvent response = parseResponse(bodyPart.getBodyPartBytes());

        switch (response.getEventType()) {
          case KeepAlive:
            break;
          case Cancel:
            deferred.reject(new FirebaseAccessException(referenceUrl));
            break;
          case Expired:
            deferred.reject(new FirebaseAuthenticationExpiredException(referenceUrl));
            break;
          default:
            deferred.notify(response);
            break;
        }

        return STATE.CONTINUE;
      }

      @Override
      public STATE onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
        LOG.info("Received Status: " + responseStatus.getStatusCode());
        switch (responseStatus.getStatusCode()) {
          // 307 = Temporary Redirect
          case 307:
          case HttpURLConnection.HTTP_OK:
            break;
          case HttpURLConnection.HTTP_UNAUTHORIZED:
          case HttpURLConnection.HTTP_FORBIDDEN:
            LOG.warn("The request to '{}' that violates the Security and Firebase Rules", referenceUrl);
            deferred.reject(new FirebaseAccessException(responseStatus));
            break;
          default:
            LOG.error("Unsupported status code: " + responseStatus.getStatusCode());
            deferred.reject(new FirebaseRestException(FirebaseRuntimeException.ErrorCode.UnsupportedStatusCode, responseStatus));
            break;
        }

        return STATE.CONTINUE;
      }

      @Override
      public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
        LOG.debug("Received headers");
        return STATE.CONTINUE;
      }

      @Override
      public Void onCompleted() throws Exception {
        LOG.info("DONE");
        deferred.resolve(null);
        return null;
      }
    };
  }

  private StreamingEvent parseResponse(byte[] response) throws IOException {

    try (ByteArrayInputStream is = new ByteArrayInputStream(response);
         BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {

      final StreamingEvent.EventType eventType = getEventType(reader.readLine());
      final String eventData = getEventData(reader.readLine());

      return new StreamingEvent(gson, eventType, eventData);
    }
  }

  private StreamingEvent.EventType getEventType(String eventString) {
    LOG.debug("Mapping event type -> " + eventString);

    // eventString format -> event: <eventType>
    return EVENT_TYPE_MAPPER.get(
      eventString.replaceFirst("event:", "").trim().toLowerCase()
    );
  }

  private String getEventData(String dataString) {
    LOG.debug("Extracting event data -> " + dataString);

    // dataString format -> data: <data>
    return dataString.replaceFirst("data:", "").trim();
  }
}
