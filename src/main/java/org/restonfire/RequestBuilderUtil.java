package org.restonfire;

import com.ning.http.client.AsyncHttpClient;

/**
 * Utility class to build the requests to Firebase.
 */
final class RequestBuilderUtil {
  private RequestBuilderUtil() {
    //do nothing
  }

  public static AsyncHttpClient.BoundRequestBuilder createGet(AsyncHttpClient asyncHttpClient, String referenceUrl, String accessToken) {
    final AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient
      .prepareGet(referenceUrl);

    return addQueryParamsIfApplicable(requestBuilder, accessToken);
  }

  public static AsyncHttpClient.BoundRequestBuilder createPost(AsyncHttpClient asyncHttpClient, String referenceUrl, String accessToken, String body) {
    final AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient
      .preparePost(referenceUrl)
      .setBody(body);

    return addQueryParamsIfApplicable(requestBuilder, accessToken);
  }

  public static AsyncHttpClient.BoundRequestBuilder createPatch(AsyncHttpClient asyncHttpClient, String referenceUrl, String accessToken, String body) {
    final AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient
      .preparePatch(referenceUrl)
      .setBody(body);

    return addQueryParamsIfApplicable(requestBuilder, accessToken);
  }

  public static AsyncHttpClient.BoundRequestBuilder createPut(AsyncHttpClient asyncHttpClient, String referenceUrl, String accessToken, String body) {
    final AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient
      .preparePut(referenceUrl)
      .setBody(body);

    return addQueryParamsIfApplicable(requestBuilder, accessToken);
  }

  public static AsyncHttpClient.BoundRequestBuilder createDelete(AsyncHttpClient asyncHttpClient, String referenceUrl, String accessToken) {
    final AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient
      .prepareDelete(referenceUrl);

    return addQueryParamsIfApplicable(requestBuilder, accessToken);
  }

  private static AsyncHttpClient.BoundRequestBuilder addQueryParamsIfApplicable(AsyncHttpClient.BoundRequestBuilder requestBuilder, String accessToken) {
    if (notNullOrEmpty(accessToken)) {
      return requestBuilder.addQueryParam("auth", accessToken);
    }

    return requestBuilder;
  }

  private static boolean notNullOrEmpty(String aString) {
    return aString != null && aString.length() > 0;
  }
}
