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
  private final String fbAccessToken = "someAccessToken";
  private final String body = "some random body";

  @Test
  public void testCreateGet() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).prepareGet(getExpectedUrl()); will(returnValue(requestBuilder));
    }});

    assertSame(requestBuilder, RequestBuilderUtil.createGet(asyncHttpClient, referenceUrl, fbAccessToken));
  }

  @Test
  public void testCreatePost() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).preparePost(getExpectedUrl()); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setBody(body); will(returnValue(requestBuilder));
    }});

    assertSame(requestBuilder, RequestBuilderUtil.createPost(asyncHttpClient, referenceUrl, fbAccessToken, body));
  }

  @Test
  public void testCreatePatch() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).preparePatch(getExpectedUrl()); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setBody(body); will(returnValue(requestBuilder));
    }});

    assertSame(requestBuilder, RequestBuilderUtil.createPatch(asyncHttpClient, referenceUrl, fbAccessToken, body));
  }

  @Test
  public void testCreatePut() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).preparePut(getExpectedUrl()); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setBody(body); will(returnValue(requestBuilder));
    }});

    assertSame(requestBuilder, RequestBuilderUtil.createPut(asyncHttpClient, referenceUrl, fbAccessToken, body));
  }

  @Test
  public void testCreateDelete() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).prepareDelete(getExpectedUrl()); will(returnValue(requestBuilder));
    }});

    assertSame(requestBuilder, RequestBuilderUtil.createDelete(asyncHttpClient, referenceUrl, fbAccessToken));
  }

  private String getExpectedUrl() {
    return referenceUrl + "?auth=" + fbAccessToken;
  }
}
