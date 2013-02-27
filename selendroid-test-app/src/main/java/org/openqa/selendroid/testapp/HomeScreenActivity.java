/*
 * Copyright 2012 selendroid committers.
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
package org.openqa.selendroid.testapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Demo project to verify NativeAndroidDriver actions.
 * 
 * @author ddary
 * 
 */
public class HomeScreenActivity extends Activity {
  private static final int DIALOG_ALERT = 10;
  private static String TAG = "Selendroid-demoapp";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i(TAG, "onCreate");
    setContentView(R.layout.homescreen);
  }

  public void showL10nDialog(View view) {
    showDialog(DIALOG_ALERT);
  }

  public void showWebViewDialog(View view) {
    Intent nextScreen = new Intent(getApplicationContext(), WebViewActivity.class);
    startActivity(nextScreen);
  }
  
  public void showUserRegistrationDialog(View view) {
    Intent nextScreen = new Intent(getApplicationContext(),RegisterUserActivity.class);
    startActivity(nextScreen);
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case DIALOG_ALERT:
        // Create out AlterDialog
        Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This will end the activity");
        builder.setCancelable(true);
        builder.setPositiveButton("I agree", new OkOnClickListener());
        builder.setNegativeButton("No, no", new CancelOnClickListener());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    return super.onCreateDialog(id);
  }

  private final class CancelOnClickListener implements DialogInterface.OnClickListener {
    public void onClick(DialogInterface dialog, int which) {
      Toast.makeText(getApplicationContext(), "Activity will continue", Toast.LENGTH_LONG).show();
    }
  }

  private final class OkOnClickListener implements DialogInterface.OnClickListener {
    public void onClick(DialogInterface dialog, int which) {
      HomeScreenActivity.this.finish();
    }
  }

}
