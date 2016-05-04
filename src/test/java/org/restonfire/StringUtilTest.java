package org.restonfire;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for StringUtil.
 */
public class StringUtilTest {

  @Test
  public void testNotNullOrEmpty_null() {
    assertFalse(StringUtil.notNullOrEmpty(null));
  }

  @Test
  public void testNotNullOrEmpty_empty() {
    assertFalse(StringUtil.notNullOrEmpty(""));
  }

  @Test
  public void testNotNullOrEmpty_notEmpty() {
    assertTrue(StringUtil.notNullOrEmpty("a"));
  }
}
