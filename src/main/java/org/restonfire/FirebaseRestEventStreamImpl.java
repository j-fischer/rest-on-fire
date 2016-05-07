package org.restonfire;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ning.http.client.*;
import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.restonfire.exceptions.FirebaseAccessException;
import org.restonfire.exceptions.FirebaseAuthenticationExpiredException;
import org.restonfire.exceptions.FirebaseRuntimeException;
import org.restonfire.responses.EventStreamResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jfischer on 2016-05-05.
 */
class FirebaseRestEventStreamImpl extends FirebaseDocumentLocation implements FirebaseRestEventStream {

  private static final Logger LOG = LoggerFactory.getLogger(FirebaseRestEventStreamImpl.class);

  private static final Map<String, EventStreamResponse.EventType> EVENT_TYPE_MAPPER;
  static {
    EVENT_TYPE_MAPPER = new HashMap<>();
    EVENT_TYPE_MAPPER.put("put", EventStreamResponse.EventType.Set);
    EVENT_TYPE_MAPPER.put("patch", EventStreamResponse.EventType.Update);
    EVENT_TYPE_MAPPER.put("keep-alive", EventStreamResponse.EventType.KeepAlive);
    EVENT_TYPE_MAPPER.put("cancel", EventStreamResponse.EventType.Cancel);
    EVENT_TYPE_MAPPER.put("auth_revoked", EventStreamResponse.EventType.Expired);
  }

  private final Gson gson;
  private final AsyncHttpClient asyncHttpClient;
  private final AsyncHttpClient.BoundRequestBuilder eventStreamRequest;

  private final Object lock = new Object();
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
  public Promise<Void, FirebaseRuntimeException, EventStreamResponse> startListening() {
    LOG.debug("startListening() invoked for reference {}", referenceUrl);
    final Deferred<Void, FirebaseRuntimeException, EventStreamResponse> deferred = new DeferredObject<>();

    final AsyncHandler<Void> asyncRequestHandler = createAsyncHandler(deferred);

    synchronized (lock) {
      if (currentListener != null) {
        // FIXME: throw exception
      }

      currentListener = eventStreamRequest.execute(asyncRequestHandler);
    }

    return deferred.promise();
  }

  @Override
  public void stopListening() {
    synchronized (lock) {
      if (currentListener == null) {
        // FIXME: throw exception
      }

      currentListener.done();
      currentListener = null;
    }
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

  private AsyncHandler<Void> createAsyncHandler(final Deferred<Void, FirebaseRuntimeException, EventStreamResponse> deferred) {
    return new AsyncHandler<Void>() {
      @Override
      public void onThrowable(Throwable t) {
        LOG.error("EventStream for location '" + referenceUrl + "' failed", t);
      }

      @Override
      public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        LOG.debug("Received event");

        final EventStreamResponse response = parseResponse(bodyPart.getBodyPartBytes());

        switch (response.getEventType()) {
          case KeepAlive:
            return STATE.CONTINUE;
          case Cancel:
            deferred.reject(new FirebaseAccessException(referenceUrl));
            return STATE.CONTINUE;
          case Expired:
            deferred.reject(new FirebaseAuthenticationExpiredException(referenceUrl));
            return STATE.CONTINUE;
          default:
            deferred.notify(response);
            return STATE.CONTINUE;
        }
      }

      @Override
      public STATE onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
        LOG.info("Received Status: " + responseStatus.getStatusCode());
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

  private EventStreamResponse parseResponse(byte[] response) throws IOException {

    try (ByteArrayInputStream is = new ByteArrayInputStream(response);
         BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {

      final EventStreamResponse.EventType eventType = getEventType(reader.readLine());
      final Map<String, Object> eventData = eventType == EventStreamResponse.EventType.Set || eventType == EventStreamResponse.EventType.Update
        ? getEventData(reader.readLine())
        : null;

      return new EventStreamResponse(eventType, eventData);
    }
  }

  private EventStreamResponse.EventType getEventType(String eventString) {
    LOG.debug("Mapping event type -> " + eventString);

    // eventString format -> event: <eventType>
    return EVENT_TYPE_MAPPER.get(
      eventString.split(":")[1].trim().toLowerCase()
    );
  }

  private Map<String, Object> getEventData(String dataString) {
    LOG.debug("Deserializing event data -> " + dataString);

    // dataString format -> data: <data>
    return gson.fromJson(
      dataString.replaceFirst("data:", "").trim(),
      new TypeToken<Map<String, Object>>() { }.getType()
    );
  }
}
