/*
 * Copyright 2013-2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.openqa.selenium.remote.ExecuteMethod;

import java.util.List;
import java.util.Map;

/**
 * A MultiTouchScreen can execute both TouchActions and MultiTouchAction's using the
 * ExecuteMethod provided to them.
 */
public class MultiTouchScreen {

  private ExecuteMethod mExecuteMethod;

  public MultiTouchScreen(ExecuteMethod executeMethod) {
    this.mExecuteMethod = executeMethod;
  }

  private Map<String, Object> createActionChain(TouchAction touchAction) {
    Map<String, Object> actionChain = Maps.newHashMap();
    actionChain.put("inputDevice", "touch");
    actionChain.put("id", "");
    actionChain.put("actions", touchAction.getActionChain());

    return actionChain;
  }

  protected void executeAction(TouchAction touchAction) {
    List<Map<String, Object>> payload = Lists.newArrayList();
    payload.add(createActionChain(touchAction));

    Map<String, Object> params = Maps.newHashMap();
    params.put("payload", payload);

    mExecuteMethod.execute("actions", params);
  }

  protected void executeAction(MultiTouchAction multiAction) {
    List<Map<String, Object>> payload = Lists.newArrayList();
    for (TouchAction touchAction : multiAction.getTouchActions()) {
      payload.add(createActionChain(touchAction));
    }

    Map<String, Object> params = Maps.newHashMap();
    params.put("payload", payload);

    mExecuteMethod.execute("actions", params);
  }
}
