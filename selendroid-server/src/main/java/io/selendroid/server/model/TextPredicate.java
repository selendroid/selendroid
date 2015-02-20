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
package io.selendroid.server.model;

import android.view.View;
import android.widget.TextView;
import com.android.internal.util.Predicate;

public class TextPredicate implements Predicate<View> {

  protected String using;

  public TextPredicate(String using) {
    this.using = using;
  }

  public boolean apply(View to) {
    if (to instanceof TextView) {
      return String.valueOf(((TextView) to).getText()).equals(using);
    }
    return false;
  }
}
