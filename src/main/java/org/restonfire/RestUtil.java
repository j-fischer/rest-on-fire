package org.restonfire;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ning.http.client.Response;
import org.restonfire.exceptions.FirebaseAccessException;
import org.restonfire.exceptions.FirebaseRestException;
import org.restonfire.exceptions.FirebaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Helper class for common REST based functions.
 */
final class RestUtil {

  private static final Logger LOG = LoggerFactory.getLogger(RestUtil.class);

  private static final String FAILED_TO_PARSE_RESPONSE_BODY_FOR_REQUEST = "Failed to parse responses body for request: ";

  private RestUtil() {
    //do nothing
  }

  public static <T> T handleResponse(Gson gson, String referenceUrl, Response response, Class<T> clazz) {
    try {
      switch (response.getStatusCode()) {
        case HttpURLConnection.HTTP_OK:
          return clazz == null
            ? null
            : gson.fromJson(response.getResponseBody(), clazz);
        case HttpURLConnection.HTTP_UNAUTHORIZED:
        case HttpURLConnection.HTTP_FORBIDDEN:
          LOG.warn("The request to '{}' that violates the Security and Firebase Rules", referenceUrl);
          throw new FirebaseAccessException(response);
        default:
          LOG.error("Unsupported status code ({}), body: ", response.getStatusCode(), response.getResponseBody());
          throw new FirebaseRestException(FirebaseRuntimeException.ErrorCode.UnsupportedStatusCode, response);
      }
    } catch (JsonSyntaxException | IOException e) {
      LOG.error(FAILED_TO_PARSE_RESPONSE_BODY_FOR_REQUEST + response.getUri(), e);
      throw new FirebaseRestException(FirebaseRuntimeException.ErrorCode.ResponseDeserializationFailure, FAILED_TO_PARSE_RESPONSE_BODY_FOR_REQUEST + response.getUri(), e);
    }
  }
}
