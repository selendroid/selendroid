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


import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Demo project to verify selendroid actions.
 * 
 * @author ddary
 * 
 */
public class HomeScreenActivity extends Activity {
  private static final int DIALOG_ALERT = 10;
  private static final int DIALOG_DOWNLOAD_PROGRESS = 11;
  private static String TAG = "Selendroid-demoapp";
  private ProgressDialog progressDialog = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i(TAG, "onCreate");
    setContentView(R.layout.homescreen);
  }

  public void showL10nDialog(View view) {
    showDialog(DIALOG_ALERT);
  }

  public void showWaitingDialog(View view) {
    new MyAsyncTask().execute("");
  }

  public void showWebViewDialog(View view) {
    Intent nextScreen = new Intent(getApplicationContext(), WebViewActivity.class);
    startActivity(nextScreen);
  }

  public void showSearchDialog(View view) {
    Intent nextScreen = new Intent(getApplicationContext(), SearchUsersActivity.class);
    startActivity(nextScreen);
  }

  public void showUserRegistrationDialog(View view) {
    Intent nextScreen = new Intent(getApplicationContext(), RegisterUserActivity.class);
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
        return dialog;
      case DIALOG_DOWNLOAD_PROGRESS:
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Waiting Dialog");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);
        progressDialog.show();
        return progressDialog;
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
  class MyAsyncTask extends AsyncTask<String, String, String> {
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      showDialog(DIALOG_DOWNLOAD_PROGRESS);
    }

    @Override
    protected String doInBackground(String... params) {
      try {
        Thread.sleep(4000);
        progressDialog.setProgress(25);
        Thread.sleep(4000);
        progressDialog.setProgress(50);
        Thread.sleep(4000);
        progressDialog.setProgress(75);
        Thread.sleep(4000);
        progressDialog.setProgress(100);
        Thread.sleep(1000);
      } catch (Exception e) {}
      return null;
    }

    @Override
    protected void onPostExecute(String unused) {
      try {
        dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
      } catch (Exception e) {
        // ignore
      }
      showUserRegistrationDialog(null);
    }

  }
}
