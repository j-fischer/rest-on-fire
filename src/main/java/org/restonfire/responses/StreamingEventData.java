package org.restonfire.responses;

/**
 * Simple Pojo for deseriazation of the Firebase REST event data.
 * @param <T> The type for the actual data behind the node.
 */
public final class StreamingEventData<T> {
  private final String path;
  private final T data;

  public StreamingEventData() {
    this(null, null);
  }

  public StreamingEventData(String path, T data) {
    this.path = path;
    this.data = data;
  }

  public String getPath() {
    return path;
  }

  public T getData() {
    return data;
  }
}
