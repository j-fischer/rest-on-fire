package org.restonfire;


import org.jdeferred.Promise;
import org.restonfire.exceptions.FirebaseRuntimeException;

/**
 * Created by jfischer on 2016-04-07.
 */
public interface FirebaseRestReference {

  String getReferenceUrl();

  <T> Promise<T, FirebaseRuntimeException, Void> getValue(Class<T> clazz);
  <T> Promise<T, FirebaseRuntimeException, Void> setValue(T value);
  <T> Promise updateValue(T value);
  <T> Promise<Void, FirebaseRuntimeException, Void> removeValue();
  Promise<FirebaseRestReference, FirebaseRuntimeException, Void> push();

  FirebaseRestReference getRoot();
  FirebaseRestReference getParent();
  FirebaseRestReference child(String path);

  // TODO: Add query functions limitToFirst, limitToLast, startAt, endAt, equalTo
  // TODO: Add orderBy
}
