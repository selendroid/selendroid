/*
 * Copyright 2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.testapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ExtremLargeActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LinearLayout myLayout = new LinearLayout(this);
    myLayout.setBackgroundColor(Color.BLUE);
    myLayout.setOrientation(LinearLayout.VERTICAL);

    for (int i = 0; i <= 400; i++) {
      LinearLayout row = new LinearLayout(this);
      row.setOrientation(LinearLayout.HORIZONTAL);
      Button myButton = new Button(this);
      myButton.setText("Press me \u2666 \u2665" + i);
      myButton.setId(28 + i);
      myButton.setBackgroundColor(Color.YELLOW);

      row.addView(myButton);
      EditText text = new EditText(this);
      text.setId(500 + i);
      text.setText("TextField with text \u2660 \u2663: " + i);
      row.addView(text);
      myLayout.addView(row);
    }
    setContentView(myLayout);
  }


}
