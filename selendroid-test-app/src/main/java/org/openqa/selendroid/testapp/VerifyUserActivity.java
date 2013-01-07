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


import org.openqa.selendroid.testapp.domain.User;
import org.openqa.selendroid.testapp.services.UserService;
import org.openqa.selendroid.testapp.services.impl.UserServiceImpl;
import org.openqa.selendroid.testapp.utils.MyServiceConection;

import android.app.Activity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Demo project to verify NativeAndroidDriver actions.
 * 
 * @author ddary
 * 
 */
public class VerifyUserActivity extends Activity {
  private static String TAG = "NativeAndroidDriver-demoapp";
  private UserService userService = null;
  private ServiceConnection con = new MyServiceConection();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i(TAG, "onCreate");
    setContentView(R.layout.verify_user);

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      User user = (User) extras.getSerializable("user");
      System.out.println(" the user " + user);
      setTextOfUiElement(user.getEmail(), R.id.label_email_data);
      setTextOfUiElement(user.getName(), R.id.label_name_data);
      setTextOfUiElement(user.getPassword(), R.id.label_password_data);
      setTextOfUiElement(user.getPreferedProgrammingLanguge(),
          R.id.label_preferedProgrammingLanguage_data);
      setTextOfUiElement(user.getUsername(), R.id.label_username_data);
      setTextOfUiElement(String.valueOf(user.isAcceptAdds()), R.id.label_acceptAdds_data);
    }
  }

  private void setTextOfUiElement(String text, int uiId) {
    ((TextView) findViewById(uiId)).setText(text);
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

  public void registerUserAfterVerificytion(View view) {
    Intent nextScreen = new Intent(getApplicationContext(), HomeScreenActivity.class);
    startActivity(nextScreen);
  }



}
