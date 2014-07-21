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
package io.selendroid.nativetests;

import io.selendroid.server.utils.CallLogWrapper;
import io.selendroid.server.utils.SingleCallLog;
import io.selendroid.support.BaseAndroidTest;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;


public class AddCallLogTest extends BaseAndroidTest {

  /**
   * @throws Exception
   */
  @Test
  public void testAddCallLogFeature() throws Exception {
      final String number = "1111111111";
      final int duration = 123;
      final int direction = SingleCallLog.INCOMING_TYPE;
      final Date date = new Date();
	  driver().addCallLog(new SingleCallLog(number,duration,date,direction));
	  CallLogWrapper callLog = driver().readCallLog();
	  Assert.assertTrue(callLog.containsLogFromNumber(number));
	  List<SingleCallLog> logs = callLog.getAllLogsOfDuration(100, true);
	  for(SingleCallLog cn : logs) {
	      Assert.assertTrue(cn.getDuration()>=100);
	  }
	  logs = callLog.getAllLogsOfDuration(100, false);
	  for(SingleCallLog cn : logs) {
	      Assert.assertTrue(cn.getDuration()<100);
	  }
  }

}
