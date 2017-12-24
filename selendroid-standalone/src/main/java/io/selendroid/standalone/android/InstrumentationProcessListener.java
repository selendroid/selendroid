package io.selendroid.standalone.android;

public interface InstrumentationProcessListener {
  void onInstrumentationProcessComplete(String output);
  void onInstrumentationProcessFailed(String output, Exception error);
}
