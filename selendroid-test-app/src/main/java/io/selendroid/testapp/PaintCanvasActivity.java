package io.selendroid.testapp;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Cameron on 3/29/2015.
 */
public class PaintCanvasActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.paint_canvas);
  }
}
