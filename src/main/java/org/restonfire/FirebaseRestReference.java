package org.restonfire;


import org.jdeferred.Promise;
import org.restonfire.exceptions.FirebaseRuntimeException;

import java.util.concurrent.Future;

/**
 * Created by jfischer on 2016-04-07.
 */
public interface FirebaseRestReference {

  <T> Promise<T, FirebaseRuntimeException, Void> getValue(Class<T> clazz);
  <T> void setValue(T value);
  <T> void updateValue(T value);
  <T> void removeValue(T value);
  Future<FirebaseRestReference> push();

  FirebaseRestReference root();
  FirebaseRestReference parent();
  FirebaseRestReference child(String path);

  // TODO: Add query functions limitToFirst, limitToLast, startAt, endAt, equalTo
  // TODO: Add orderBy
}
