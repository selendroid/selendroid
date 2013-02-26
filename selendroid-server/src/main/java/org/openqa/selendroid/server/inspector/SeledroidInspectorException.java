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
package org.openqa.selendroid.server.inspector;

import org.openqa.selendroid.server.exceptions.SelendroidException;

public class SeledroidInspectorException extends SelendroidException {
  private static final long serialVersionUID = 6848294719850875948L;

  public SeledroidInspectorException(String message, Throwable t) {
    super(message, t);
  }

  public SeledroidInspectorException(Throwable t) {
    super(t);
  }

  public SeledroidInspectorException(String message) {
    super(message);
  }
}
