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
package org.openqa.selendroid.testapp.services.impl;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selendroid.testapp.domain.User;
import org.openqa.selendroid.testapp.services.UserService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Just a simple service for handling user registration and sign in.
 * 
 * @author ddary
 * 
 */
public class UserServiceImpl extends Service implements UserService {
  private Map<String, User> users = new HashMap<String, User>();

  @Override
  public IBinder onBind(Intent intent) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public User signUserIn(String username, String password) {
    if (username == null || password == null) {
      throw new RuntimeException("invalid parameter");
    }
    if (users.containsKey(username)) {
      User user = users.get(username);
      if (password.equals(user.getPassword())) {
        return user;
      }
    }
    throw new RuntimeException("User does not exist");
  }

  @Override
  public User registerUser(User userToRegister) {
    if (userToRegister == null) {
      throw new RuntimeException("invalid parameter");
    }
    validateUser(userToRegister);
    users.put(userToRegister.getUsername(), userToRegister);
    return userToRegister;
  }

  @Override
  public void validateUser(User user) {
    if (user == null) {
      throw new IllegalArgumentException("user object cannot be null");
    }
    if (user.getEmail().isEmpty()) {
      throw new RuntimeException("email empty");
    }
    if (user.getUsername().isEmpty()) {
      throw new RuntimeException("username empty");
    }
  }

}
