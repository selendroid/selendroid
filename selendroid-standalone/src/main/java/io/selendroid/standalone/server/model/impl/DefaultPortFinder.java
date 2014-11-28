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
package io.selendroid.standalone.server.model.impl;

import io.selendroid.standalone.server.model.EmulatorPortFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultPortFinder implements EmulatorPortFinder {
  private List<Integer> availablePorts = new ArrayList<Integer>();
  private List<Integer> portsInUse = new ArrayList<Integer>();
  private Integer minPort;
  private Integer maxPort;

  public DefaultPortFinder(Integer minPort, Integer maxPort) {
    this.minPort = minPort;
    this.maxPort = maxPort;
    for (int i = minPort; i <= maxPort; i++) {
      if (isEvenNumber(i)) {
        availablePorts.add(i);
      }
    }
  }

  /* (non-Javadoc)
   * @see io.selendroid.server.model.impl.EmulatorPortFinder#next()
   */
  @Override
  public synchronized Integer next() {
    if (availablePorts.isEmpty()) {
      return null;
    }
    Collections.sort(availablePorts);
    Integer port = availablePorts.get(0);
    portsInUse.add(port);
    availablePorts.remove(port);
    return port;
  }

  /* (non-Javadoc)
   * @see io.selendroid.server.model.impl.EmulatorPortFinder#release(java.lang.Integer)
   */
  @Override
  public synchronized void release(Integer port) {
    if(port != null){
      portsInUse.remove(port);
      if (port >= minPort && port <= maxPort && isEvenNumber(port)) {
        availablePorts.add(port);
      }
    }
  }

  private boolean isEvenNumber(Integer port) {
    if (port == null) {
      return false;
    }
    return ((port % 2) == 0);
  }
}
