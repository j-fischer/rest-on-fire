package org.restonfire.testutils;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;

/**
 * Helper class for unit tests using mock objects.
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

  protected void addExpectations(Expectations expectations) {
    context.checking(expectations);
  }

  @After
  public void assertIsSatisfied() {
    context.assertIsSatisfied();
  }
}
