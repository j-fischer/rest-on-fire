package org.restonfire.impl;

import org.restonfire.FirebaseRestFactory;
import org.restonfire.FirebaseRestReference;

/**
 * Created by jfischer on 2016-04-07.
 */
public class FirebaseRequestFactory implements FirebaseRestFactory {
  private static FirebaseRequestFactory ourInstance = new FirebaseRequestFactory();

  public static FirebaseRequestFactory getInstance() {
    return ourInstance;
  }

  private boolean isDebugModeEnabled = false;

  private FirebaseRequestFactory() {
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
