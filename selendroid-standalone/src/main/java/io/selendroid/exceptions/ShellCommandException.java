/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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
package io.selendroid.exceptions;

public class ShellCommandException extends Exception {
  private static final long serialVersionUID = 268831360479853360L;

  public ShellCommandException(String message) {
    super(message);
  }

  public ShellCommandException(Throwable t) {
    super(t);
  }

  public ShellCommandException(String message, Throwable t) {
    super(message, t);
  }
}
