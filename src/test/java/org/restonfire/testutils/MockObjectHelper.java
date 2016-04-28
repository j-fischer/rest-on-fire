package org.restonfire.testutils;

import org.apache.commons.lang3.mutable.MutableObject;
import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Helper functions to enhance the behavior of mock objects.
 */
public class MockObjectHelper
{
  public static Action runRunnable()
  {
    return runRunnable(0);
  }

  public static Action runRunnable(final int argumentIndex)
  {
    return new Action() {

      public Object invoke(Invocation invocation) throws Throwable
      {
        final Runnable runnable = (Runnable) invocation.getParameter(argumentIndex);
        runnable.run();
        return null;
      }

      public void describeTo(Description description)
      {
        description.appendText("Run a runnable");
      }
    };
  }

  public static Action callCallable()
  {
    return callCallable(0);
  }

  public static Action callCallable(final int argumentIndex)
  {
    return new Action() {

      public Object invoke(Invocation invocation) throws Throwable
      {
        final Callable callable = (Callable) invocation.getParameter(argumentIndex);
        return callable.call();
      }

      public void describeTo(Description description)
      {
        description.appendText("Run a runnable");
      }
    };
  }

  public static <T> Action capture(final MutableObject<T> obj)
  {
    return capture(obj, 0);
  }

  public static <T> Action capture(final MutableObject obj, int argumentIndex)
  {
    return new CaptureAction<T>(argumentIndex, obj);
  }

  public static <K, V> Action addElementsToMap(Map<K, V> newElements)
  {
    return new AddElementsAction<K, V>(newElements);
  }

  private static class CaptureAction<T> implements Action
  {
    private final MutableObject<T> mutable;
    private final int argumentIndex;

    public CaptureAction(Integer argumentIndex, MutableObject mutable)
    {
      assert mutable != null : "mutable object cannot be null";
      assert argumentIndex != null : "argument index cannot be null";

      this.mutable = mutable;
      this.argumentIndex = argumentIndex;
    }

    public Object invoke(Invocation invocation) throws Throwable
    {
      mutable.setValue((T) invocation.getParameter(argumentIndex));
      return null;
    }

    public void describeTo(Description description)
    {
      description.appendText("capture-to MutableObject");
    }
  }

  private static class AddElementsAction<TKey, TVal> implements Action
  {
    private Map<TKey, TVal> elements;

    public AddElementsAction(Map<TKey, TVal> elements)
    {
      this.elements = elements;
    }

    public void describeTo(Description description)
    {
      description.appendText("adds ")
        .appendValueList("", ", ", "", elements)
        .appendText(" to a collection");
    }

    public Object invoke(Invocation invocation) throws Throwable
    {
      ((Map<TKey, TVal>)invocation.getParameter(0)).putAll(elements);
      return null;
    }
  }
}
