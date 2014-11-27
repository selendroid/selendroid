package io.selendroid.server.android;

import android.view.MotionEvent;

public interface MotionSender {

  boolean send(Iterable<MotionEvent> events);

}
