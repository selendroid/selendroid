package io.selendroid.testapp.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Paint canvas view for manual testing on touch gestures.
 *
 * @author colindmurray
 * @author chooper9
 */
public class FingerView extends View {
  private Paint paint;
  private HashMap<Integer, Path> pointerPathMap;

  public FingerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    paint = new Paint();
    paint.setColor(Color.WHITE);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(12);
    pointerPathMap = new HashMap<Integer, Path>();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    // A pointer's index may change throughout touch events, so track id
    int idx = event.getActionIndex();
    int id = event.getPointerId(idx);
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN:
        Path pth = new Path();
        pth.moveTo(event.getX(idx), event.getY(idx));
        pointerPathMap.put(id, pth);
        break;
      case MotionEvent.ACTION_MOVE:
        // Update all pointers since ACTION_MOVE events don't provide a pointer index
        for(Map.Entry<Integer, Path> entry : pointerPathMap.entrySet()) {
          idx = event.findPointerIndex(entry.getKey());
          entry.getValue().lineTo(event.getX(idx), event.getY(idx));
        }
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:
        pointerPathMap.remove(id);
        break;
    }
    invalidate();
    return true;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    for(Path p : pointerPathMap.values()) {
      canvas.drawPath(p, paint);
    }
  }
}
