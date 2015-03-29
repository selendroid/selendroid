package io.selendroid.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.TextView;

/**
 * Activity for testing selendroid touch gestures.
 *
 * @author colindmurray
 * @author chooper9
 */
public class TouchGesturesActivity extends Activity
        implements GestureDetector.OnGestureListener, 
        GestureDetector.OnDoubleTapListener {

    private TextView gestureTypeTV;
    private TextView scaleFactorTV;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private GestureDetectorCompat gestureDetect;
    private ScaleGestureDetector scaleDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestures_demo);
        gestureTypeTV = (TextView) findViewById(R.id.gesture_type_text_view);
        scaleFactorTV = (TextView) findViewById(R.id.scale_factor_text_view);
        textView3 = (TextView) findViewById(R.id.text_view3);
        textView4 = (TextView) findViewById(R.id.text_view4);
        textView5 = (TextView) findViewById(R.id.text_view5);
        gestureDetect = new GestureDetectorCompat(this, this);
        gestureDetect.setIsLongpressEnabled(true);
        scaleDetector = new ScaleGestureDetector(this, new MyScaleListener());
    }


    private void clearExtraInformationTextViews() {
        textView3.setText("");
        textView4.setText("");
        textView5.setText("");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        clearExtraInformationTextViews();
        scaleDetector.onTouchEvent(event);
        // Check if multitouch action or single touch based on pointer count.
        if(event.getPointerCount() > 1) {
            clearExtraInformationTextViews();
            gestureTypeTV.setText("MULTI TOUCH EVENT");
            textView3.setText("Num Pointers: " + event.getPointerCount());
            int action = MotionEventCompat.getActionMasked(event);
            textView4.setText(actionToString(action));
            int index = MotionEventCompat.getActionIndex(event);
            textView5.setText("Pointer index: " + index);
        }
        else
            gestureDetect.onTouchEvent(event);

        return true;
    }


    @Override
    public boolean onDown(MotionEvent e) {
        gestureTypeTV.setText("DOWN");
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        String display = "FLICK";
        textView3.setText("vx: " + velocityX + " pps");
        textView4.setText("vy: " + velocityY + " pps");
        gestureTypeTV.setText(display);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        gestureTypeTV.setText("LONG PRESS");
    }

    @Override
    public boolean onScroll (MotionEvent e1, MotionEvent e2, 
            float distanceX, float distanceY) {
        gestureTypeTV.setText("SCROLL");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        gestureTypeTV.setText("SHOW PRESS");       
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        gestureTypeTV.setText("SINGLE TAP UP");
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent arg0) {
        gestureTypeTV.setText("DOUBLE TAP");
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent arg0) {
        gestureTypeTV.setText("ON DOUBLE TAP EVENT");
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent arg0) {
        gestureTypeTV.setText("SINGLE TAP CONFIRMED");
        return true;
    }

    public void startCanvasActivity(View view) {
      Intent intent = new Intent(getApplicationContext(), PaintCanvasActivity.class);
      startActivity(intent);
    }

    // FROM http://developer.android.com/training/gestures/multi.html
    // Given an action int, returns a string description
    public static String actionToString(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN: return "Down";
            case MotionEvent.ACTION_MOVE: return "Move";
            case MotionEvent.ACTION_POINTER_DOWN: return "Pointer Down";
            case MotionEvent.ACTION_UP: return "Up";
            case MotionEvent.ACTION_POINTER_UP: return "Pointer Up";
            case MotionEvent.ACTION_OUTSIDE: return "Outside";
            case MotionEvent.ACTION_CANCEL: return "Cancel";
        }
        return "";
    }


    // from http://developer.android.com/training/gestures/scale.html
    private class MyScaleListener 
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactorTV.setText("" + detector.getScaleFactor());
            return true;
        }


    }

}
