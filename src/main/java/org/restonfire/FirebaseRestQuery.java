package org.restonfire;

import org.jdeferred.Promise;
import org.restonfire.exceptions.FirebaseRuntimeException;

/**
 * TODO: Add JavaDocs
 * Created by jfischer on 2016-05-28.
 */
public interface FirebaseRestQuery {

  // TODO: Implement remaining methods
  FirebaseRestQuery startAt(Object val);
  FirebaseRestQuery endAt(Object val);

  FirebaseRestQuery equalTo(Object val);

  FirebaseRestQuery limitToFirst(int number);
  FirebaseRestQuery limitToLast(int number);

  FirebaseRestQuery orderByKey();
  FirebaseRestQuery orderByChild(String name);
  FirebaseRestQuery orderByPriority();
  FirebaseRestQuery orderByValue();

  <T> Promise<T, FirebaseRuntimeException, Void> run(Class<T> clazz);
}
