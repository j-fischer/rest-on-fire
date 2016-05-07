package org.restonfire.responses;

import java.util.Map;

/**
 * Created by jfischer on 2016-05-06.
 */
public class EventStreamResponse {

  private final EventType eventType;
  private final Map<String, Object> eventData;

  public EventStreamResponse(EventType eventType, Map<String, Object> eventData) {
    this.eventType = eventType;
    this.eventData = eventData;
  }

  public EventType getEventType() {
    return eventType;
  }

  public Map<String, Object> getEventData() {
    return eventData;
  }

  /**
   * Enum describing the different event types to be returned by the Firebase streaming service.
   *
   * @see <a href="https://www.firebase.com/docs/rest/api/#section-streaming">Streaming from Firebase REST API</a>
   */
  public enum EventType {
    Set,
    Update,
    KeepAlive,
    Cancel,
    Expired
  }
}
