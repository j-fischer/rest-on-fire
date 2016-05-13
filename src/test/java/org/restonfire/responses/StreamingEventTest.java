package org.restonfire.responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import org.restonfire.testdata.SampleData;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link StreamingEvent}.
 */
public class StreamingEventTest {

  private final Gson gson = new GsonBuilder().create();

  private final int intVal = new Integer(4);
  private final double intAsDouble = (double) intVal;
  private final SampleData sampleData = new SampleData("foo", 2);
  private final Map<String, Object> sampleDataAsMap = sampleData.toAutoDeserializedMap();

  private final String intResponseStr = "{ path: '/', data: 4 }";
  private final String sampleDataString = "{ path: '/', data: " + gson.toJson(sampleData) + "}";

  @Test
  public void testGetSerialzedEventData() {
    assertEquals(intResponseStr, createRespose(StreamingEvent.EventType.Set, intResponseStr).getSerialzedEventData());
    assertEquals(sampleDataString, createRespose(StreamingEvent.EventType.Set, sampleDataString).getSerialzedEventData());
  }

  @Test
  public void testGetEventData_set_deserializedValues() {
    assertEquals(intAsDouble, (Object) createRespose(StreamingEvent.EventType.Set, intResponseStr).getEventData().getData());
    assertEquals(sampleDataAsMap, createRespose(StreamingEvent.EventType.Set, sampleDataString).getEventData().getData());
  }

  @Test
  public void testGetEventData_update_deserializedValues() {
    assertEquals(intAsDouble, (Object) createRespose(StreamingEvent.EventType.Update, intResponseStr).getEventData().getData());
    assertEquals(sampleDataAsMap, createRespose(StreamingEvent.EventType.Update, sampleDataString).getEventData().getData());
  }

  @Test
  public void testGetEventData_typed() {
    assertEquals(sampleData, createRespose(StreamingEvent.EventType.Set, sampleDataString).getEventData(new TypeToken<StreamingEventData<SampleData>>() { }).getData());
    assertEquals(sampleData, createRespose(StreamingEvent.EventType.Update, sampleDataString).getEventData(new TypeToken<StreamingEventData<SampleData>>() { }).getData());
  }

  @Test
  public void testGetEventData_returnsNullForAllOtherEventTypes() {
    assertNull(createRespose(StreamingEvent.EventType.Cancel, intResponseStr).getEventData());
    assertNull(createRespose(StreamingEvent.EventType.Expired, intResponseStr).getEventData());
    assertNull(createRespose(StreamingEvent.EventType.KeepAlive, intResponseStr).getEventData());
  }

  @Test
  public void testGetEventData_checkEventResponseDataPath() {
    assertEquals("/", createRespose(StreamingEvent.EventType.Update, intResponseStr).getEventData().getPath());
  }


  private StreamingEvent createRespose(StreamingEvent.EventType eventType, String eventData) {
    return new StreamingEvent(gson, eventType, eventData);
  }
}
