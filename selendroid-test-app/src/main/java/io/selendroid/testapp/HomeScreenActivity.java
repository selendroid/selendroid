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
package io.selendroid.testapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Demo project to verify selendroid actions.
 * 
 * @author ddary
 */
public class HomeScreenActivity extends Activity {
  private static final int DIALOG_ALERT = 10;
  private static final int DIALOG_LONG_PRESS = 12;
  private static final int DIALOG_DOWNLOAD_PROGRESS = 11;
  private static String TAG = "Selendroid-demoapp";
  private ProgressDialog progressDialog = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i(TAG, "onCreate");
    setContentView(io.selendroid.testapp.R.layout.homescreen);
    Button button = (Button) findViewById(io.selendroid.testapp.R.id.buttonTest);
    button.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        showDialog(DIALOG_LONG_PRESS);
        return true;
      }
    });
    initExceptionTestButton();
    initExceptionTestField();
  }


  @Override
  protected void onResume() {
    TextView textview = ((TextView) findViewById(io.selendroid.testapp.R.id.visibleTextView));

    textview.setVisibility(View.INVISIBLE);

    super.onResume();
  }

  public void displayPopupWindow(View view) {
    LayoutInflater layoutInflater =
        (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
    View popupView = layoutInflater.inflate(io.selendroid.testapp.R.layout.popup_window, null);
    final PopupWindow popupWindow =
        new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    Button btnDismiss =
        (Button) popupView.findViewById(io.selendroid.testapp.R.id.popup_dismiss_button);
    btnDismiss.setOnClickListener(new Button.OnClickListener() {

      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        popupWindow.dismiss();
      }
    });
    Button btnOpenPopup = (Button) findViewById(io.selendroid.testapp.R.id.showPopupWindowButton);
    popupWindow.showAsDropDown(btnOpenPopup, -50, -300);
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

    public void showTouchActionsDialog(View view) {
        Intent nextScreen = new Intent(getApplicationContext(), TouchGesturesActivity.class);
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

  public void displayToast(View view) {
    Context context = getApplicationContext();
    CharSequence text = "Hello selendroid toast!";
    int duration = Toast.LENGTH_LONG;

    Toast toast = Toast.makeText(context, text, duration);
    toast.show();
  }

  public void displayTextView(View view) {
    TextView textview = ((TextView) findViewById(io.selendroid.testapp.R.id.visibleTextView));
    if (textview.isShown()) {
      textview.setVisibility(View.INVISIBLE);
    } else {
      textview.setVisibility(View.VISIBLE);
    }
  }

  public void displayAndFocus(View view) {
    LinearLayout linearLayout =
        ((LinearLayout) findViewById(io.selendroid.testapp.R.id.focusedLayout));
    if (linearLayout.isShown()) {
      linearLayout.setVisibility(View.GONE);
    } else {
      linearLayout.setVisibility(View.VISIBLE);
      linearLayout.requestFocus();
    }
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
      case DIALOG_LONG_PRESS:
        Builder builder2 = new AlertDialog.Builder(this);
        builder2.setMessage("Long Press Tap has been received.");
        builder2.setCancelable(true);
        builder2.setPositiveButton("Ok", new CancelOnClickListener());
        AlertDialog dialog2 = builder2.create();
        dialog2.show();
        return dialog2;
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
        Thread.sleep(3000);
        progressDialog.setProgress(25);
        Thread.sleep(3000);
        progressDialog.setProgress(50);
        Thread.sleep(3000);
        progressDialog.setProgress(75);
        Thread.sleep(3000);
        progressDialog.setProgress(100);
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(io.selendroid.testapp.R.layout.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case io.selendroid.testapp.R.id.menu_home:
        startActivity(new Intent(getApplicationContext(), HomeScreenActivity.class));
        return true;

      case io.selendroid.testapp.R.id.menu_web_view:
        startActivity(new Intent(getApplicationContext(), WebViewActivity.class));
        return true;

      case io.selendroid.testapp.R.id.menu_multiple_web_views:
        startActivity(new Intent(getApplicationContext(), MultipleWebViewsActivity.class));
        return true;
      case io.selendroid.testapp.R.id.extreem_large_view:
        startActivity(new Intent(getApplicationContext(), ExtremLargeActivity.class));
        return true;
      case io.selendroid.testapp.R.id.menu_find_employee:
        Intent intent = new Intent("android.intent.action.MAIN");
        intent
            .setComponent(ComponentName
                .unflattenFromString("io.selendroid.directory/io.selendroid.directory.EmployeeDirectoy"));
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setData(Uri.parse("http://selendroid.io/directory#employees/4"));
        startActivity(intent);

        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void initExceptionTestButton() {
    Button exceptionTestButton =
        (Button) findViewById(io.selendroid.testapp.R.id.exceptionTestButton);
    exceptionTestButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        throw new RuntimeException("Unhandled Exception Test!");
      }
    });
  }

  private void initExceptionTestField() {
    EditText exceptionTestField =
        (EditText) findViewById(io.selendroid.testapp.R.id.exceptionTestField);
    exceptionTestField.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {}

      @Override
      public void afterTextChanged(Editable editable) {
        throw new RuntimeException("Unhandled Exception Test!");
      }
    });
  }
}
