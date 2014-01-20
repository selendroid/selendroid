package io.selendroid.android;

import android.view.MotionEvent;

public interface MotionSender {

  boolean send(Iterable<MotionEvent> events);

}
