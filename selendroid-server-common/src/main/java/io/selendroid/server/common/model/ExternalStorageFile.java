package io.selendroid.server.common.model;

public enum ExternalStorageFile {
  APP_CRASH_LOG("appcrash.log");

  private String fileName;

  private ExternalStorageFile(String fileName) {
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }

  @Override
  public String toString() {
    return fileName;
  }
}
