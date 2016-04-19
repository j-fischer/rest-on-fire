package org.restonfire.responses;

/**
 * * Simple Pojo for serialization/deseriazation of the Firebase push responses for REST requests.
 *
 * Created by jfischer on 2016-04-14.
 */
public final class PushResponse {
  private final String name;

  public PushResponse() {
    this("");
  }

  public PushResponse(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
