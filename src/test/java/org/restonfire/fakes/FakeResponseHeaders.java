package org.restonfire.fakes;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.HttpResponseHeaders;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Mock class for HttpResponseHeaders.
 */
public class FakeResponseHeaders extends HttpResponseHeaders {
  @Override
  public FluentCaseInsensitiveStringsMap getHeaders() {
    throw new NotImplementedException("getHeaders");
  }
}
