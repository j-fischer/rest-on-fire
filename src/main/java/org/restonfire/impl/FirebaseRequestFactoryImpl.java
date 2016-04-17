package org.restonfire.impl;

import org.restonfire.FirebaseRestFactory;
import org.restonfire.FirebaseRestReference;

/**
 * {@link FirebaseRestFactory} implementation.
 *
 * Created by jfischer on 2016-04-07.
 */
public final class FirebaseRequestFactoryImpl implements FirebaseRestFactory {

  public FirebaseRequestFactoryImpl() {
    // do nothing
  }

  @Override
  public FirebaseRestReference getReference(String path) {
    return new FirebaseRestReferenceImpl(
      null,
      null,
      null,
      null,
      path
    );
  }
}
