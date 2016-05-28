package org.restonfire;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ning.http.client.AsyncHttpClient;
import org.jmock.Expectations;
import org.junit.Test;
import org.restonfire.testutils.AbstractMockTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test class for FirebaseRestNamespaceImpl.
 */
public class FirebaseRestDatabaseImplTest extends AbstractMockTestCase {

  private final AsyncHttpClient asyncHttpClient = mock(AsyncHttpClient.class);
  private final AsyncHttpClient.BoundRequestBuilder requestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);

  private final Gson gson = new GsonBuilder().create();

  private final String path = "foo/bar";
  private final String fbBaseUrl = "https://mynamespace.firebaseio.com";
  private final String fbAccessToken = "someAccessToken";

  private final FirebaseRestDatabaseImpl namespace = new FirebaseRestDatabaseImpl(asyncHttpClient, gson, fbBaseUrl, fbAccessToken);

  @Test
  public void testGetReference() {
    FirebaseRestReference result = namespace.getReference(path);

    assertNotNull(result);
    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH + path, result.getReferenceUrl());
  }

  @Test
  public void testGetEventStream() {
    addExpectations(new Expectations() {{
      oneOf(asyncHttpClient).prepareGet("https://mynamespace.firebaseio.com/foo/bar.json"); will(returnValue(requestBuilder));
      oneOf(requestBuilder).addHeader("Accept", "text/event-stream"); will(returnValue(requestBuilder));
      oneOf(requestBuilder).addQueryParam("auth", fbAccessToken); will(returnValue(requestBuilder));
      oneOf(requestBuilder).setFollowRedirects(true); will(returnValue(requestBuilder));
    }});

    FirebaseRestEventStream result = namespace.getEventStream(path);

    assertNotNull(result);
    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH + path, result.getReferenceUrl());
  }

  @Test
  public void testGetSecurityRules() {
    FirebaseSecurityRulesReference result = namespace.getSecurityRules();

    assertNotNull(result);
  }
}
