package org.restonfire.fakes;

import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Response;
import com.ning.http.client.uri.Uri;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;

/**
 * Mock class for HttpResponseStatus.
 */
public class FakeResponseStatus extends HttpResponseStatus {

  private final int statusCode;

  public FakeResponseStatus(String url, int statusCode) {
    super(Uri.create(url), null);
    this.statusCode = statusCode;
  }

  @Override
  public int getStatusCode() {
    return statusCode;
  }

  @Override
  public Response prepareResponse(HttpResponseHeaders headers, List<HttpResponseBodyPart> bodyParts) {
    throw new NotImplementedException("prepareResponse");
  }

  @Override
  public String getStatusText() {
    throw new NotImplementedException("getStatusText");
  }

  @Override
  public String getProtocolName() {
    throw new NotImplementedException("getProtocolName");
  }

  @Override
  public int getProtocolMajorVersion() {
    throw new NotImplementedException("getProtocolMajorVersion");
  }

  @Override
  public int getProtocolMinorVersion() {
    throw new NotImplementedException("getProtocolMinorVersion");
  }

  @Override
  public String getProtocolText() {
    throw new NotImplementedException("getProtocolText");
  }
}
