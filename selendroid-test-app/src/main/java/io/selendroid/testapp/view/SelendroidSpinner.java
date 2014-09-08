/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.selendroid.testapp.view;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.Spinner;

public class SelendroidSpinner extends Spinner {
  private int lastSelected = 0;
  private static Method s_pSelectionChangedMethod = null;

  static {
    try {
      Class noparams[] = {};
      Class targetClass = AdapterView.class;

      s_pSelectionChangedMethod = targetClass.getDeclaredMethod("selectionChanged", noparams);
      if (s_pSelectionChangedMethod != null) {
        s_pSelectionChangedMethod.setAccessible(true);
      }

    } catch (Exception e) {
      Log.e("Custom spinner, reflection bug:", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public SelendroidSpinner(Context context) {
    super(context);
  }

  public SelendroidSpinner(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SelendroidSpinner(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void testReflectionForSelectionChanged() {
    try {
      Class noparams[] = {};
      s_pSelectionChangedMethod.invoke(this, noparams);
    } catch (Exception e) {
      Log.e("Custom spinner, reflection bug: ", e.getMessage());
      e.printStackTrace();
    }
  }

  private Object ob = null; // class level variable

  @Override
  public boolean onTouchEvent(MotionEvent m) {
    if (m.getAction() == MotionEvent.ACTION_DOWN) {
      ob = this.getSelectedItem();
    }
    return super.onTouchEvent(m);
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    super.onClick(dialog, which);
    if (this.getSelectedItem().equals(ob)) testReflectionForSelectionChanged();
  }
}
