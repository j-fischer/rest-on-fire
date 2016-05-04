package org.restonfire;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ning.http.client.AsyncHttpClient;
import org.junit.Test;
import org.restonfire.testutils.AbstractMockTestCase;

import static org.junit.Assert.assertEquals;

/**
 * Test class for FirebaseRestNamespaceImpl.
 */
public class FirebaseRestNamespaceImplTest extends AbstractMockTestCase {

  private final AsyncHttpClient asyncHttpClient = mock(AsyncHttpClient.class);

  private final Gson gson = new GsonBuilder().create();

  private final String path = "foo/bar";
  private final String fbBaseUrl = "https://mynamespace.firebaseio.com";
  private final String fbAccessToken = "someAccessToken";

  private final FirebaseRestNamespaceImpl namespace = new FirebaseRestNamespaceImpl(asyncHttpClient, gson, fbBaseUrl, fbAccessToken);

  @Test
  public void testGetReference() {
    FirebaseRestReference result = namespace.getReference(path);

    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH + path, result.getReferenceUrl());
  }
}
