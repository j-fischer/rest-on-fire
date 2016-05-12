package org.restonfire.responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.restonfire.testdata.SampleData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link EventStreamResponse}.
 */
public class EventStreamResponseTest {

  private final Gson gson = new GsonBuilder().create();

  private final int intVal = new Integer(4);
  private final SampleData sampleData = new SampleData("foo", 2);

  private final String intResponseStr = gson.toJson(intVal);
  private final String sampleDataString = gson.toJson(sampleData);

  @Test
  public void testGetSerialzedEventData() {
    assertEquals(intResponseStr, createRespose(EventStreamResponse.EventType.Set, intResponseStr).getSerialzedEventData());
    assertEquals(sampleDataString, createRespose(EventStreamResponse.EventType.Set, sampleDataString).getSerialzedEventData());
  }

  @Test
  public void testGetEventData__set_deserializedValues() {
    assertEquals(intVal, (Object) createRespose(EventStreamResponse.EventType.Set, intResponseStr).getEventData(Integer.class));
    assertEquals(sampleData, createRespose(EventStreamResponse.EventType.Set, sampleDataString).getEventData(SampleData.class));
  }

  @Test
  public void testGetEventData__update_deserializedValues() {
    assertEquals(intVal, (Object) createRespose(EventStreamResponse.EventType.Update, intResponseStr).getEventData(Integer.class));
    assertEquals(sampleData, createRespose(EventStreamResponse.EventType.Update, sampleDataString).getEventData(SampleData.class));
  }

  @Test
  public void testGetEventData__returnsNullForAllOtherEventTypes() {
    assertNull(createRespose(EventStreamResponse.EventType.Cancel, intResponseStr).getEventData(SampleData.class));
    assertNull(createRespose(EventStreamResponse.EventType.Expired, intResponseStr).getEventData(SampleData.class));
    assertNull(createRespose(EventStreamResponse.EventType.KeepAlive, intResponseStr).getEventData(SampleData.class));
  }

  private EventStreamResponse createRespose(EventStreamResponse.EventType eventType, String eventData) {
    return new EventStreamResponse(gson, eventType, eventData);
  }
}
