package org.restonfire.utils;

import com.ning.http.client.AsyncHttpClient;
import org.jmock.Expectations;
import org.junit.Test;
import org.restonfire.testutils.AbstractMockTestCase;

import static org.junit.Assert.assertSame;

/**
 * Created by jfischer on 2016-04-24.
 */
public class RequestBuilderUtilTest extends AbstractMockTestCase {

  private final AsyncHttpClient asyncHttpClient = mock(AsyncHttpClient.class);
  private final AsyncHttpClient.BoundRequestBuilder requestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);

  private final String referenceUrl = "https://www.some-domain.com";
  private final String body = "some random body";

  @Test
  public void testCreateGet() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).prepareGet(referenceUrl); will(returnValue(requestBuilder));
    }});

    assertSame(requestBuilder, RequestBuilderUtil.createGet(asyncHttpClient, referenceUrl));
  }

  @Test
  public void testCreatePost() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).preparePost(referenceUrl); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setBody(body); will(returnValue(requestBuilder));
    }});

    assertSame(requestBuilder, RequestBuilderUtil.createPost(asyncHttpClient, referenceUrl, body));
  }

  @Test
  public void testCreatePatch() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).preparePatch(referenceUrl); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setBody(body); will(returnValue(requestBuilder));
    }});

    assertSame(requestBuilder, RequestBuilderUtil.createPatch(asyncHttpClient, referenceUrl, body));
  }

  @Test
  public void testCreatePut() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).preparePut(referenceUrl); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setBody(body); will(returnValue(requestBuilder));
    }});

    assertSame(requestBuilder, RequestBuilderUtil.createPut(asyncHttpClient, referenceUrl, body));
  }

  @Test
  public void testCreateDelete() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).prepareDelete(referenceUrl); will(returnValue(requestBuilder));
    }});

    assertSame(requestBuilder, RequestBuilderUtil.createDelete(asyncHttpClient, referenceUrl));
  }
}
