package org.restonfire.testutils;

import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

/**
 * Helper class for unit tests using mock objects.
 *
 * Created by jfischer on 2016-04-20.
 */
public abstract class AbstractMockTestCase {

  protected Mockery context = new Mockery() {{
    setImposteriser(ClassImposteriser.INSTANCE);
  }};

  protected <T> T mock(Class<T> clazz) {
    return context.mock(clazz);
  }

  protected <T> T mock(Class<T> clazz, String name) {
    return context.mock(clazz, name);
  }

  protected void assertIsSatisfied() {
    context.assertIsSatisfied();
  }
}
