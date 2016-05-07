package org.restonfire;

/**
 * Created by jfischer on 2016-05-07.
 */
abstract class FirebaseDocumentLocation {

  public static final String JSON_SUFFIX = ".json";

  protected final String path;
  protected final String fbBaseUrl;
  protected final String fbAccessToken;
  protected final String referenceUrl;

  public FirebaseDocumentLocation(String fbBaseUrl, String path, String fbAccessToken) {
    this.fbBaseUrl = fbBaseUrl;
    this.path = path;
    this.fbAccessToken = fbAccessToken;

    this.referenceUrl = PathUtil.concatenatePath(fbBaseUrl, path) + JSON_SUFFIX;
  }

  public String getReferenceUrl() {
    return referenceUrl.substring(0, referenceUrl.length() - JSON_SUFFIX.length());
  }

}
