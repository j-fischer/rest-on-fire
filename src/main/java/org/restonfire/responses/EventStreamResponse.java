package org.restonfire.responses;

import com.google.gson.Gson;

/**
 * Created by jfischer on 2016-05-06.
 */
public class EventStreamResponse {

  private final Gson gson;

  private final EventType eventType;
  private final String eventData;

  public EventStreamResponse(Gson gson, EventType eventType, String eventData) {
    this.gson = gson;

    this.eventType = eventType;
    this.eventData = eventData;
  }

  public EventType getEventType() {
    return eventType;
  }

  public String getSerialzedEventData() {
    return eventData;
  }

  public <T> T getEventData(Class<T> clazz) {
    return eventType == EventStreamResponse.EventType.Set || eventType == EventStreamResponse.EventType.Update
      ? gson.fromJson(eventData, clazz)
      : null;
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
