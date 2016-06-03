package org.restonfire;

import org.jdeferred.Promise;
import org.restonfire.exceptions.FirebaseRuntimeException;

/**
 * Created by jfischer on 2016-05-28.
 */
public interface FirebaseRestQuery {

  // TODO: Implement remaining methods
//  FirebaseRestQuery startAt();
//  FirebaseRestQuery endAt();
//
//  FirebaseRestQuery equalTo();
//
//  FirebaseRestQuery limitToFirst(int number);
//  FirebaseRestQuery limitToLast(int number);

//  FirebaseRestQuery orderByKey();
//  FirebaseRestQuery orderByChild();
//  FirebaseRestQuery orderByPriority();
  FirebaseRestQuery orderByValue();

  <T> Promise<T, FirebaseRuntimeException, Void> run(Class<T> clazz);
}
