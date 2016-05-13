package org.restonfire.testdata;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple POJO to be used as a more complex document data value.
 */
public class SampleData {
  public String aString;
  public int anInt;

  public SampleData() {
    // do nothing
  }

  public SampleData(String aString, int anInt) {
    this.aString = aString;
    this.anInt = anInt;
  }

  /*
   * This function converts this SampleData object to a Map as it would be
   * returned by Gson if no type parameter was provided for the deserialization.
   */
  public Map<String,Object> toAutoDeserializedMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("aString", aString);
    map.put("anInt", (double) anInt);

    return map;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SampleData that = (SampleData) o;

    if (anInt != that.anInt) return false;
    return aString != null ? aString.equals(that.aString) : that.aString == null;
  }

  @Override
  public int hashCode() {
    int result = aString != null ? aString.hashCode() : 0;
    result = 31 * result + anInt;
    return result;
  }
}