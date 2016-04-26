package org.restonfire.utils;

import com.ning.http.client.AsyncHttpClient;

/**
 * Created by jfischer on 2016-04-14.
 */
public final class RequestBuilderUtil {
  private RequestBuilderUtil() {
    //do nothing
  }

  public static AsyncHttpClient.BoundRequestBuilder createGet(AsyncHttpClient asyncHttpClient, String referenceUrl, String accessToken) {
    return asyncHttpClient
      .prepareGet(getFullUrl(referenceUrl, accessToken));
  }

  public static AsyncHttpClient.BoundRequestBuilder createPost(AsyncHttpClient asyncHttpClient, String referenceUrl, String accessToken, String body) {
    return asyncHttpClient
      .preparePost(getFullUrl(referenceUrl, accessToken))
      .setBody(body);
  }

  public static AsyncHttpClient.BoundRequestBuilder createPatch(AsyncHttpClient asyncHttpClient, String referenceUrl, String accessToken, String body) {
    return asyncHttpClient
      .preparePatch(getFullUrl(referenceUrl, accessToken))
      .setBody(body);
  }

  public static AsyncHttpClient.BoundRequestBuilder createPut(AsyncHttpClient asyncHttpClient, String referenceUrl, String accessToken, String body) {
    return asyncHttpClient
      .preparePut(getFullUrl(referenceUrl, accessToken))
      .setBody(body);
  }

  public static AsyncHttpClient.BoundRequestBuilder createDelete(AsyncHttpClient asyncHttpClient, String referenceUrl, String accessToken) {
    return asyncHttpClient
      .prepareDelete(getFullUrl(referenceUrl, accessToken));
  }

  private static String getFullUrl(String referenceUrl, String accessToken) {
    return referenceUrl + "?auth=" + accessToken;
  }
}
