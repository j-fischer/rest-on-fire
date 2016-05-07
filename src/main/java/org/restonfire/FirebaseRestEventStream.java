package org.restonfire;

import org.jdeferred.Promise;
import org.restonfire.exceptions.FirebaseRuntimeException;
import org.restonfire.responses.EventStreamResponse;

/**
 * Created by jfischer on 2016-05-05.
 */
public interface FirebaseRestEventStream {

  Promise<Void, FirebaseRuntimeException, EventStreamResponse> startListening();

  void stopListening();

}
