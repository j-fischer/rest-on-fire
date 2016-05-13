package org.restonfire.responses;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Simple Pojo for deseriazation of the Firebase streaming event using REST.
 * The underlying event data can be retrieved in a serialized or deserialized form,
 * using the {@link Gson} instance to convert the data string.
 */
public class StreamingEvent {

  private final Gson gson;

  private final EventType eventType;
  private final String eventData;

  public StreamingEvent(Gson gson, EventType eventType, String eventData) {
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
   * @return A JSON String representing the path and the value for this event.
   */
  public String getSerialzedEventData() {
    return eventData;
  }

  /**
   * Returns the {@link StreamingEventData} without additional type context information. This means that
   * {@link Gson} will make a best effort to deserialize the response. As a result, all complex types will be represented
   * as {@link java.util.Map}&lt;String, Object&gt; and {@link Integer} values will be returned as {@link Double}.
   *
   * @return The {@link StreamingEventData} object containing the relative path and the value of the event.
   *
   * @see <a href="http://stackoverflow.com/questions/21920436/object-autoconvert-to-double-with-serialization-gson">A more detailed explanation</a>
   */
  public StreamingEventData getEventData() {
    return eventType == EventType.Set || eventType == EventType.Update
      ? gson.fromJson(eventData, StreamingEventData.class)
      : null;
  }

  /**
   * Returns the {@link StreamingEventData} without additional type context information. This means that
   * {@link Gson} will make a best effort to deserialize the response. As a result, all complex types will be represented
   * as {@link java.util.Map}&lt;String, Object&gt; and {@link Integer} values will be returned as {@link Double}.
   *
   * @param typeToken The {@link TypeToken} representing the {@link StreamingEventData} type of the data property
   *                  of the event response, including the type parameter.
   *
   * @param <T> The type of the actual data node.
   *
   * @return The {@link StreamingEventData} object containing the relative path and the value of the event.
   */
  public <T> StreamingEventData<T> getEventData(TypeToken<StreamingEventData<T>> typeToken) {
    return eventType == EventType.Set || eventType == EventType.Update
      ? gson.<StreamingEventData<T>>fromJson(eventData, typeToken.getType())
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
