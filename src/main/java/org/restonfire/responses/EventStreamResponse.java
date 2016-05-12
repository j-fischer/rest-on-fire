package org.restonfire.responses;

import com.google.gson.Gson;

/**
 * Simple Pojo for deseriazation of the Firebase streaming event using REST.
 * The underlying event data can be retrieved in a serialized or deserialized form,
 * using the {@link Gson} instance to convert the data string.
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

  /**
   * Returns the {@link EventType} for the individual event. Note that some types have been
   * renamed to match the corresponding {@link org.restonfire.FirebaseRestReference} interface
   * better.
   *
   * @return enum value for the {@link EventType}
   *
   * @see <a href="https://www.firebase.com/docs/rest/api/#section-streaming">REST Streaming Documentation</a> for more information.
   */
  public EventType getEventType() {
    return eventType;
  }

  /**
   * Returns the raw string of the data value as it was returned by the REST API.
   *
   * @return A String representing the value for this event
   */
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
   * @see <a href="https://www.firebase.com/docs/rest/api/#section-streaming">REST Streaming Documentation</a> for more information.
   */
  public enum EventType {
    Set,
    Update,
    KeepAlive,
    Cancel,
    Expired
  }
}
