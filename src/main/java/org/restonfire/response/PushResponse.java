package org.restonfire.response;

/**
 * * Simple Pojo for serialization/deseriazation of the Firebase push response for REST requests.
 *
 * Created by jfischer on 2016-04-14.
 */
public class PushResponse {
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
