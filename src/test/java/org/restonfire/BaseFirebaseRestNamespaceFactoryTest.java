package org.restonfire;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ning.http.client.AsyncHttpClient;
import org.junit.Test;
import org.restonfire.testutils.AbstractMockTestCase;

import static org.junit.Assert.assertEquals;

/**
 * Test class for BaseFirebaseRestNamespaceFactory.
 */
public class BaseFirebaseRestNamespaceFactoryTest extends AbstractMockTestCase {

  private final AsyncHttpClient asyncHttpClient = mock(AsyncHttpClient.class);

  private final Gson gson = new GsonBuilder().create();

  private final String path = "foo/bar";
  private final String fbBaseUrl = "https://mynamespace.firebaseio.com";
  private final String fbAccessToken = "someAccessToken";

  private final FirebaseRestNamespaceFactory factory = new BaseFirebaseRestNamespaceFactory(asyncHttpClient, gson);

  @Test
  public void testGetReference_withAccessToken() {
    executeCreateTest(fbBaseUrl, fbAccessToken);
  }

  @Test
  public void testGetReference_withoutAccessToken() {
    executeCreateTest(fbBaseUrl, null);
  }

  private void executeCreateTest(String fbBaseUrl, String fbAccessToken) {
    FirebaseRestNamespace namespace = factory.create(fbBaseUrl, fbAccessToken);

    FirebaseRestReference result = namespace.getReference(path);

    assertEquals(this.fbBaseUrl + PathUtil.FORWARD_SLASH + path, result.getReferenceUrl());
  }
}
