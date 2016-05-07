package org.restonfire.integrationTests

import org.jdeferred.ProgressCallback
import org.restonfire.FirebaseRestEventStream
import org.restonfire.FirebaseRestNamespace
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

  def "Get event stream"() {
    AsyncConditions cond = new AsyncConditions();

    FirebaseRestEventStream<String> eventStream = namespace.getEventStream("testData/text");
    when: "making starting to listen"

    eventStream
      .startListening()
      .progress(new ProgressCallback() {
      @Override
      void onProgress(Object progress) {
        EventStreamResponse event = (EventStreamResponse) progress;

        cond.evaluate {
        assert event.getEventType() == EventStreamResponse.EventType.Set
        assert event.getEventData() != null
      }
      }
    })
//      .always({ Promise.State state, Map<String, Object> val, FirebaseRuntimeException ex ->
//      cond.evaluate {
//        assert ex == null
//        assert val == null
//      }
//    })
    then: "wait for result evaluation"
    cond.await(60);
  }
}
