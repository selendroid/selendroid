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
package io.selendroid.nativetests;

import io.selendroid.server.common.utils.CallLogEntry;
import io.selendroid.server.common.utils.CallLogUtil;
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
      final int direction = CallLogEntry.INCOMING_TYPE;
      final Date date = new Date();
	  driver().addCallLog(new CallLogEntry(number,duration,date,direction));
	  List<CallLogEntry> callLogs = driver().readCallLog();
	  Assert.assertTrue(CallLogUtil.containsLogFromNumber(callLogs, number));
	  List<CallLogEntry> logs = CallLogUtil.getAllLogsOfDuration(callLogs, 100, true);
	  for(CallLogEntry cn : logs) {
	      Assert.assertTrue(cn.getDuration()>=100);
	  }
	  logs = CallLogUtil.getAllLogsOfDuration(callLogs, 100, false);
	  for(CallLogEntry cn : logs) {
	      Assert.assertTrue(cn.getDuration()<100);
	  }
  }

}
