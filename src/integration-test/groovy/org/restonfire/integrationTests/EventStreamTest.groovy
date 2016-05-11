package org.restonfire.integrationTests

import org.jdeferred.ProgressCallback
import org.jdeferred.Promise
import org.restonfire.FirebaseRestEventStream
import org.restonfire.FirebaseRestNamespace
import org.restonfire.exceptions.FirebaseRuntimeException
import org.restonfire.responses.EventStreamResponse
import spock.util.concurrent.AsyncConditions
/**
 * Created by jfischer on 2016-05-05.
 */
class EventStreamTest extends AbstractTest {

  private FirebaseRestNamespace namespace;

  void setup() {
    // Run this inside the setup to ensure that the setup function in the AbstractTest class is completed before the
    // namespace is crated. This ensures that the token will be created
    namespace = createNamespace()
  }

  def "Get event stream - initial event"() {
    AsyncConditions progressCondition = new AsyncConditions();
    AsyncConditions finalCondition = new AsyncConditions();

    FirebaseRestEventStream<String> eventStream = namespace.getEventStream("testData/text");
    when: "starting to listen"

    eventStream
      .startListening()
      .progress(new ProgressCallback() {
        @Override
        void onProgress(Object progress) {
          EventStreamResponse event = (EventStreamResponse) progress;

          progressCondition.evaluate {
            assert event.getEventType() == EventStreamResponse.EventType.Set
            assert event.getSerialzedEventData() != "aString"
          }
        }
      })
      .always({ Promise.State state, Map<String, Object> val, FirebaseRuntimeException ex ->
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
}
