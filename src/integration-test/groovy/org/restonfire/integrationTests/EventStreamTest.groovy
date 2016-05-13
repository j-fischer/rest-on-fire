package org.restonfire.integrationTests

import org.jdeferred.ProgressCallback
import org.jdeferred.Promise
import org.restonfire.FirebaseRestEventStream
import org.restonfire.FirebaseRestNamespace
import org.restonfire.exceptions.FirebaseRuntimeException
import org.restonfire.responses.StreamingEvent
import org.restonfire.responses.StreamingEvent.EventType
import spock.util.concurrent.AsyncConditions
/**
 * Verifies streaming events against from real Firebase namespace.
 */
class EventStreamTest extends AbstractTest {

  private FirebaseRestNamespace namespace;

  void setup() {
    // Run this inside the setup to ensure that the setup function in the AbstractTest class is completed before the
    // namespace is crated. This ensures that the token will be created
    namespace = createNamespace()
  }

  def "Event stream - initial event"() {
    AsyncConditions progressCondition = new AsyncConditions();
    AsyncConditions finalCondition = new AsyncConditions();

    FirebaseRestEventStream eventStream = namespace.getEventStream("testData/text");
    when: "starting to listen"

    eventStream
      .startListening()
      .progress(new ProgressCallback<StreamingEvent>() {
        @Override
        void onProgress(StreamingEvent event) {

          progressCondition.evaluate {
            assert event.getEventType() == StreamingEvent.EventType.Set
            assert event.getEventData().getPath() == "/"
            assert event.getEventData().getData() == "aString"
          }
        }
      })
      .always({ Promise.State state, Void val, FirebaseRuntimeException ex ->
        finalCondition.evaluate {
          assert ex == null
          assert val == null
        }
      })
    then: "wait for result evaluation"
    progressCondition.await(5);

    eventStream.stopListening()
    finalCondition.await(5);
  }

  def "Event stream - event changing value type"() {
    AsyncConditions[] progressConditions = [ new AsyncConditions(), new AsyncConditions() ]
    AsyncConditions finalCondition = new AsyncConditions()
    int eventIndex = 0

    FirebaseRestEventStream eventStream = namespace.getEventStream("testData/toBeSet");
    when: "starting to listen"

    eventStream
      .startListening()
      .progress(new ProgressCallback<StreamingEvent>() {
        @Override
        void onProgress(StreamingEvent event) {

          if (eventIndex == 0) {
            progressConditions[eventIndex++].evaluate {
              assert event.getEventType() == EventType.Set
              assert event.getEventData().getPath() == "/"
              assert event.getEventData().getData() == "SET ME"

              // Change the type to an integer
              namespace.getReference("testData/toBeSet").setValue(5)
            }
          } else {
            progressConditions[eventIndex++].evaluate {
              assert event.getEventType() == EventType.Set
              assert event.getEventData().getPath() == "/"
              assert event.getEventData().getData() == 5.0
            }
          }
        }
      })
      .always({ Promise.State state, Void val, FirebaseRuntimeException ex ->
        finalCondition.evaluate {
          assert ex == null
          assert val == null
        }
      })
    then: "wait for result evaluation"
    progressConditions[0].await(5);
    progressConditions[1].await(5);

    eventStream.stopListening()
    finalCondition.await(5);
  }

  def "Event stream - event for update of partial value"() {
    AsyncConditions[] progressConditions = [ new AsyncConditions(), new AsyncConditions() ]
    AsyncConditions finalCondition = new AsyncConditions()
    int eventIndex = 0

    namespace.getReference("testData/toBeUpdated").updateValue([boo: "baz"])

    FirebaseRestEventStream eventStream = namespace.getEventStream("testData/toBeUpdated");
    when: "starting to listen"

    eventStream
      .startListening()
      .progress(new ProgressCallback<StreamingEvent>() {
        @Override
        void onProgress(StreamingEvent event) {

          if (eventIndex == 0) {
            progressConditions[eventIndex++].evaluate {
              assert event.getEventType() == EventType.Set
              assert event.getEventData().getPath() == "/"
              assert event.getEventData().getData() == [foo: "bar", boo: "baz"]

              // Change the type to an integer
              namespace.getReference("testData/toBeUpdated").updateValue([foo: "foobar"])
            }
          } else {
            progressConditions[eventIndex++].evaluate {
              assert event.getEventType() == EventType.Update
              assert event.getEventData().getPath() == "/"
              assert event.getEventData().getData() == [foo: "foobar"]
            }
          }
        }
      })
      .always({ Promise.State state, Void val, FirebaseRuntimeException ex ->
        finalCondition.evaluate {
          assert ex == null
          assert val == null
        }
      })
    then: "wait for result evaluation"
    progressConditions[0].await(5);
    progressConditions[1].await(5);

    eventStream.stopListening()
    finalCondition.await(5);
  }

  def "Event stream - event where child path was set"() {
    AsyncConditions[] progressConditions = [ new AsyncConditions(), new AsyncConditions() ]
    AsyncConditions finalCondition = new AsyncConditions()
    int eventIndex = 0

    namespace.getReference("testData/toBeUpdated").updateValue([boo: [biz: "baz"] ])

    FirebaseRestEventStream eventStream = namespace.getEventStream("testData/toBeUpdated");
    when: "starting to listen"

    eventStream
      .startListening()
      .progress(new ProgressCallback<StreamingEvent>() {
        @Override
        void onProgress(StreamingEvent event) {

          if (eventIndex == 0) {
            progressConditions[eventIndex++].evaluate {
              assert event.getEventType() == EventType.Set
              assert event.getEventData().getPath() == "/"
              assert event.getEventData().getData() == [foo: "bar", boo: [biz: "baz"]]

              // Change the type to an integer
              namespace.getReference("testData/toBeUpdated/boo").setValue([biz: "Something New"])
            }
          } else {
            progressConditions[eventIndex++].evaluate {
              assert event.getEventType() == EventType.Set
              assert event.getEventData().getPath() == "/boo"
              assert event.getEventData().getData() == [biz: "Something New"]
            }
          }
        }
      })
      .always({ Promise.State state, Void val, FirebaseRuntimeException ex ->
        finalCondition.evaluate {
          assert ex == null
          assert val == null
        }
      })
    then: "wait for result evaluation"
    progressConditions[0].await(5);
    progressConditions[1].await(5);

    eventStream.stopListening()
    finalCondition.await(5);
  }
}
