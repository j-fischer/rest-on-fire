package org.restonfire;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ning.http.client.AsyncHttpClient;
import org.junit.Test;
import org.restonfire.testutils.AbstractMockTestCase;
import org.restonfire.utils.PathUtil;

import static org.junit.Assert.assertEquals;

/**
 * Created by jfischer on 2016-04-24.
 */
public class BaseFirebaseRestNamespaceFactoryTest extends AbstractMockTestCase {

  private final AsyncHttpClient asyncHttpClient = mock(AsyncHttpClient.class);

  private final Gson gson = new GsonBuilder().create();

  private final String path = "foo/bar";
  private final String fbBaseUrl = "https://mynamespace.firebaseio.com";
  private final String fbAccessToken = "someAccessToken";

  private final FirebaseRestNamespace factory = BaseFirebaseRestNamespaceFactory.create(asyncHttpClient, gson, fbBaseUrl, fbAccessToken);

  @Test
  public void testGetReference() {
    FirebaseRestReference result = factory.getReference(path);

    assertEquals(fbBaseUrl + PathUtil.FORWARD_SLASH + path, result.getReferenceUrl());
  }
}
