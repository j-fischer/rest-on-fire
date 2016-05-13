package org.restonfire.responses;

/**
 * Simple Pojo for serialization/deseriazation of the Firebase push responses for REST requests.
 */
public final class PushResponse {
  private final String name;

  private PushResponse() {
    this("");
  }

  public PushResponse(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
