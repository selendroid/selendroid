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
package org.openqa.selendroid.testapp.domain;

import java.io.Serializable;

public class User implements Serializable {
  private static final long serialVersionUID = 1365748034232111845L;
  private boolean acceptAdds;
  private String email;
  private String name;
  private String password;
  private String preferedProgrammingLanguge;
  private String username;

  public User(String username, String password) {
    super();
    this.username = username;
    this.password = password;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    User other = (User) obj;
    if (password == null) {
      if (other.password != null) return false;
    } else if (!password.equals(other.password)) return false;
    if (username == null) {
      if (other.username != null) return false;
    } else if (!username.equals(other.username)) return false;
    return true;
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  public String getPassword() {
    return password;
  }

  public String getPreferedProgrammingLanguge() {
    return preferedProgrammingLanguge;
  }

  public String getUsername() {
    return username;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((password == null) ? 0 : password.hashCode());
    result = prime * result + ((username == null) ? 0 : username.hashCode());
    return result;
  }

  public boolean isAcceptAdds() {
    return acceptAdds;
  }

  public void setAcceptAdds(boolean acceptAdds) {
    this.acceptAdds = acceptAdds;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setPreferedProgrammingLanguge(String preferedProgrammingLanguge) {
    this.preferedProgrammingLanguge = preferedProgrammingLanguge;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public String toString() {
    return "User [name=" + name + ", username=" + username + ", password=" + password + ", email="
        + email + ", preferedProgrammingLanguge=" + preferedProgrammingLanguge + ", acceptAdds="
        + acceptAdds + "]";
  }


}
