package io.selendroid.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

public class GesturesDemo extends Activity 
        implements GestureDetector.OnGestureListener, 
        GestureDetector.OnDoubleTapListener {

    private TextView gestureTypeTV;
    private TextView scaleFactorTV;
    private GestureDetectorCompat gestureDetect;
    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1.f;
    private int textViewScaleFontSize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestures_demo);
        gestureTypeTV = (TextView) findViewById(R.id.gesture_type);
        scaleFactorTV = (TextView) findViewById(R.id.scale_factor_tv);
        textViewScaleFontSize = (int) getResources().getDimension(R.dimen.scroll_text_view_text_size);
        gestureDetect = new GestureDetectorCompat(this, this); // GACKY
        gestureDetect.setIsLongpressEnabled(true);
        scaleDetector = new ScaleGestureDetector(this, new MyScaleListener());
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) { 
        scaleDetector.onTouchEvent(event);
        if(event.getPointerCount() > 1) {
            gestureTypeTV.setText("MULTI TOUCH\nEVENT");
            gestureTypeTV.append("\nNum Pointers: " + event.getPointerCount());
            int action = MotionEventCompat.getActionMasked(event); 
            gestureTypeTV.append("\n" + actionToString(action));
            int index = MotionEventCompat.getActionIndex(event);
            gestureTypeTV.append("\nPointer index: "+ index);
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
        //display += "dx: " + velocityX + " pps\n";
        //display += "dy: " + velocityY + " pps";
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
        gestureTypeTV.setText("SINGLE TAP\nCONFIRMED");
        return true;
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
            // Log.d("GESTURE DEMO", "Scale factor: " + detector.getScaleFactor());
            scaleFactor *= detector.getScaleFactor();
            Log.d("GESTURE DEMO", "scaleFactor: " + scaleFactor + " getScaleFactor(): " + detector.getScaleFactor());
            // Log.d("GESTURE DEMO", "Scale factor calculated: " + scaleFactor);
            scaleFactor = Math.max(0.001f, Math.min(scaleFactor, 10.0f));
            // Log.d("GESTURE DEMO", "Scale factor clamped: " + scaleFactor);
            double scaleFactorDisplay = ((int) (scaleFactor * 1000)) / 1000.0;

            scaleFactorTV.setText("SCALE FACTOR: " + scaleFactorDisplay);
            handleTextSize();
            return true;
        }

        
        private void handleTextSize() {
            int newTextSize = Math.round(textViewScaleFontSize * scaleFactor);

            if(newTextSize < 4)
                newTextSize = 4;
            else if(newTextSize > 36)
                newTextSize = 36;
            Log.d("GESTURE DEMO", "textViewScaleFontSize: " + textViewScaleFontSize);
            Log.d("GESTURE DEMO", "newTextSize: " + newTextSize);
            scaleFactorTV.setTextSize(newTextSize);
        }

    }
}
