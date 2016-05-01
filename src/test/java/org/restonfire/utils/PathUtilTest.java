package org.restonfire.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * Test class for PathUtil.
 */
public class PathUtilTest {

  private static final String PATH = "foo/bar";
  private static final String PATH_WITH_SLASH = "foo/bar/";

  private static final String SINGLE_PATH = "foo";

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void getParent_pathIsNull() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("path cannot be null");

    PathUtil.getParent(null);
  }

  @Test
  public void getParent_pathIsEmptyString() throws Exception {
    assertEquals(null, PathUtil.getParent(""));
  }

  @Test
  public void getParent_pathIsSingleSlash() throws Exception {
    assertEquals(null, PathUtil.getParent("/"));
  }

  @Test
  public void getParent_singleCharPath() throws Exception {
    assertEquals("", PathUtil.getParent("a"));
  }

  @Test
  public void getParent_singleCharPathWithSlash() throws Exception {
    assertEquals("", PathUtil.getParent("a/"));
  }

  @Test
  public void getParent_pathEndsWithSlash() throws Exception {
    assertEquals("foo", PathUtil.getParent(PATH_WITH_SLASH));
  }

  @Test
  public void getParent_pathDoesNotEndWithSlash() throws Exception {
    assertEquals("foo", PathUtil.getParent(PATH));
  }

  @Test
  public void getChild_pathIsNull() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("path cannot be null");

    PathUtil.concatenatePath(null, SINGLE_PATH);
  }

  @Test
  public void getChild_ofRootPath() throws Exception {
    assertEquals("foo", PathUtil.concatenatePath("", SINGLE_PATH));
  }

  @Test
  public void getChild_ofRootPathWithSlash() throws Exception {
    assertEquals("foo", PathUtil.concatenatePath("/", SINGLE_PATH));
  }

  @Test
  public void getChild_ofExistingPath() throws Exception {
    assertEquals("foo/bar/foo", PathUtil.concatenatePath(PATH, SINGLE_PATH));
  }

  @Test
  public void getChild_ofExistingPathWithSlash() throws Exception {
    assertEquals("foo/bar/foo", PathUtil.concatenatePath(PATH_WITH_SLASH, SINGLE_PATH));
  }

  @Test
  public void getChild_ofExistingPathWithWithMultileveChild() throws Exception {
    assertEquals("foo/bar/foo/bar", PathUtil.concatenatePath(PATH, PATH));
    assertEquals("foo/bar/foo/bar", PathUtil.concatenatePath(PATH_WITH_SLASH, PATH_WITH_SLASH));
  }

  @Test
  public void normalizePath() {
    assertEquals(PATH, PathUtil.normalizePath(PATH));
    assertEquals(PATH, PathUtil.normalizePath(PATH_WITH_SLASH));
  }
}