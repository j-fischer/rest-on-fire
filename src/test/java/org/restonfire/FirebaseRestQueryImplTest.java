package org.restonfire;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Param;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jmock.Expectations;
import org.junit.Test;
import org.restonfire.testutils.AbstractMockTestCase;
import org.restonfire.testutils.MockObjectHelper;

import java.util.Arrays;

/**
 * Unit tests for FirebaseRestQueryImpl.
 */
public class FirebaseRestQueryImplTest extends AbstractMockTestCase {

  private final AsyncHttpClient.BoundRequestBuilder requestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);

  private final Gson gson = new GsonBuilder().create();
  private final MutableObject<AsyncCompletionHandler<Void>> capturedCompletionHandler = new MutableObject<>();

  private final String referenceUrl = "https://mynamespace.firebaseio.com/some/path";

  private final FirebaseRestQueryImpl query = new FirebaseRestQueryImpl(gson, requestBuilder, referenceUrl);

  @Test
  public void testStartAt_StringValue() {
    String someVal = "abc";

    query.startAt(someVal);

    expectRequestexecution(new Param("startAt", gson.toJson(someVal)));

    query.run(String.class);
  }

  @Test
  public void testStartAt_BooleanValue() {
    query.startAt(true);

    expectRequestexecution(new Param("startAt", gson.toJson(true)));

    query.run(Boolean.class);
  }

  @Test
  public void testEndAt_StringValue() {
    String someVal = "abc";

    query.endAt(someVal);

    expectRequestexecution(new Param("endAt", gson.toJson(someVal)));

    query.run(String.class);
  }

  @Test
  public void testEndAt_BooleanValue() {
    query.endAt(true);

    expectRequestexecution(new Param("endAt", gson.toJson(true)));

    query.run(Boolean.class);
  }

  @Test
  public void testEqualTo_StringValue() {
    String someVal = "abc";

    query.equalTo(someVal);

    expectRequestexecution(new Param("equalTo", gson.toJson(someVal)));

    query.run(String.class);
  }

  @Test
  public void testEqualTo_BooleanValue() {
    query.equalTo(true);

    expectRequestexecution(new Param("equalTo", gson.toJson(true)));

    query.run(Boolean.class);
  }

  @Test
  public void testLimitToFirst() {
    query.limitToFirst(3);

    expectRequestexecution(new Param("limitToFirst", gson.toJson(3)));

    query.run(String.class);
  }

  @Test
  public void testLimitToLast() {
    query.limitToLast(5);

    expectRequestexecution(new Param("limitToLast", gson.toJson(5)));

    query.run(String.class);
  }

  @Test
  public void testOrderByChild() {
    final String childName = "foo";

    query.orderByChild(childName);

    expectRequestexecution(new Param("orderBy", gson.toJson(childName)));

    query.run(String.class);
  }

  @Test
  public void testOrderByKey() {
    query.orderByKey();

    expectRequestexecution(new Param("orderBy", gson.toJson("$key")));

    query.run(String.class);
  }

  @Test
  public void testOrderByPriority() {
    query.orderByPriority();

    expectRequestexecution(new Param("orderBy", gson.toJson("$priority")));

    query.run(String.class);
  }

  @Test
  public void testOrderByValue() {
    query.orderByValue();

    expectRequestexecution(new Param("orderBy", gson.toJson("$value")));

    query.run(String.class);
  }

  @Test
  public void testFullQuery() {
    query
      .orderByValue()
      .startAt("bar")
      .endAt("foo")
      .limitToFirst(10);

    expectRequestexecution(
      new Param("orderBy", gson.toJson("$value")),
      new Param("limitToFirst", gson.toJson(10)),
      new Param("startAt", gson.toJson("bar")),
      new Param("endAt", gson.toJson("foo"))
    );

    query.run(String.class);
  }

  private void expectRequestexecution(Param... params) {
    expectParams(params);

    addExpectations(new Expectations() {{
      oneOf(requestBuilder).execute(with(any(AsyncCompletionHandler.class))); will(MockObjectHelper.capture(capturedCompletionHandler));
    }});
  }

  private void expectParams(final Param[] params) {
    addExpectations(new Expectations() {{
      oneOf(requestBuilder).addQueryParams(with(MockObjectHelper.elementsAreEqual(Arrays.asList(params))));
    }});
  }
}
