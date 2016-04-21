package org.restonfire;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ning.http.client.AsyncHttpClient;
import org.junit.Test;
import org.restonfire.testutils.AbstractMockTestCase;

import static org.junit.Assert.assertEquals;

/**
 * Test class for FirebaseRestReferenceImpl.
 *
 * Created by jfischer on 2016-04-20.
 */
public class FirebaseRestReferenceImplTest extends AbstractMockTestCase {

  private final AsyncHttpClient asyncHttpClient = mock(AsyncHttpClient.class);

  private final Gson gson = new GsonBuilder().create();
  private final String fbBaseUrl = "httos://mynamespace.firebaseio.com";
  private final String fbAccessToken = "fbAccessToken";
  private final String path = "foo/par";

  private final FirebaseRestReferenceImpl ref = new FirebaseRestReferenceImpl(
    asyncHttpClient,
    gson,
    fbBaseUrl,
    fbAccessToken,
    path
  );

  @Test
  public void testGetReferenceUrl() {
    assertEquals(fbBaseUrl + path, ref.getReferenceUrl());
  }

}
