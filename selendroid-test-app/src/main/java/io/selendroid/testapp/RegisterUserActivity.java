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

import io.selendroid.testapp.domain.User;
import io.selendroid.testapp.services.UserService;
import io.selendroid.testapp.services.impl.UserServiceImpl;
import io.selendroid.testapp.utils.MyServiceConection;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;


/**
 * Demo project to verify NativeAndroidDriver actions.
 * 
 * @author ddary
 * 
 */
public class RegisterUserActivity extends Activity {
  private static String TAG = "selendroid-demoapp";
  private static final int DIALOG_ALERT = 11;
  private UserService userService = null;
  private ServiceConnection con = new MyServiceConection();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i(TAG, "onCreate");
    setContentView(io.selendroid.testapp.R.layout.register_user);
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

      case io.selendroid.testapp.R.id.menu_settings:
        showDialog(DIALOG_ALERT);
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onResume() {
    Intent intent = new Intent(this, UserServiceImpl.class);
    bindService(intent, con, BIND_AUTO_CREATE);
    super.onResume();
  }

  @Override
  protected void onPause() {
    unbindService(con);
    super.onPause();
  }

  public void registerUser(View view) {
    Intent nextScreen = new Intent(getApplicationContext(), VerifyUserActivity.class);
    String username =
        ((EditText) findViewById(io.selendroid.testapp.R.id.inputUsername)).getText().toString();
    String email =
        ((EditText) findViewById(io.selendroid.testapp.R.id.inputEmail)).getText().toString();
    String password =
        ((EditText) findViewById(io.selendroid.testapp.R.id.inputPassword)).getText().toString();
    String name =
        ((EditText) findViewById(io.selendroid.testapp.R.id.inputName)).getText().toString();
    boolean acceptAdds =
        ((CheckBox) findViewById(io.selendroid.testapp.R.id.input_adds)).isChecked();
    String preferedProgrammingLanguge =
        ((Spinner) findViewById(io.selendroid.testapp.R.id.input_preferedProgrammingLanguage))
            .getSelectedItem().toString();

    User user = new User(username, password);
    user.setEmail(email);
    user.setName(name);
    user.setAcceptAdds(acceptAdds);
    user.setPreferedProgrammingLanguge(preferedProgrammingLanguge);

    nextScreen.putExtra("user", user);
    startActivity(nextScreen);
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case DIALOG_ALERT:
        // Create out AlterDialog
        Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Personal data will be stored to improve the product.\n\nDo you agree?");
        builder.setCancelable(true);
        builder.setPositiveButton("I don't", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            RegisterUserActivity.this.finish();
          }
        });
        builder.setNegativeButton("I agree", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    return super.onCreateDialog(id);
  }
}
