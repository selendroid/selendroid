package io.selendroid.server.common.utils;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class CallLogUtilTest extends TestCase {
    private CallLogUtil callLogUtil = new CallLogUtil();

    public void testGetAllLogsOfDurationGreaterThan() {
        List<CallLogEntry> logs = new ArrayList<CallLogEntry>();
        CallLogEntry callLogEntry10 = new CallLogEntry("0000000000", 10);
        CallLogEntry callLogEntry11 = new CallLogEntry("1111111111", 11);
        CallLogEntry callLogEntry12 = new CallLogEntry("2222222222", 12);
        CallLogEntry callLogEntry13 = new CallLogEntry("3333333333", 13);
        logs.add(callLogEntry10);
        logs.add(callLogEntry11);
        logs.add(callLogEntry12);
        logs.add(callLogEntry13);

        int duration = 12;
        boolean greaterthan = true;

        List<CallLogEntry> expectedResult = new ArrayList<CallLogEntry>();
        expectedResult.add(callLogEntry12);
        expectedResult.add(callLogEntry13);

        List<CallLogEntry> result = callLogUtil.getAllLogsOfDuration(logs, duration, greaterthan);
        assertArrayEquals(result.toArray(), expectedResult.toArray());
    }

    public void testGetAllLogsOfDurationLessThan() {
        List<CallLogEntry> logs = new ArrayList<CallLogEntry>();
        CallLogEntry callLogEntry10 = new CallLogEntry("0000000000", 10);
        CallLogEntry callLogEntry11 = new CallLogEntry("1111111111", 11);
        CallLogEntry callLogEntry12 = new CallLogEntry("2222222222", 12);
        CallLogEntry callLogEntry13 = new CallLogEntry("3333333333", 13);
        logs.add(callLogEntry10);
        logs.add(callLogEntry11);
        logs.add(callLogEntry12);
        logs.add(callLogEntry13);

        int duration = 12;
        boolean greaterthan = false;

        List<CallLogEntry> expectedResult = new ArrayList<CallLogEntry>();
        expectedResult.add(callLogEntry10);
        expectedResult.add(callLogEntry11);

        List<CallLogEntry> result = callLogUtil.getAllLogsOfDuration(logs, duration, greaterthan);
        assertArrayEquals(result.toArray(), expectedResult.toArray());
    }

    public void getContainsLogFromNumber() {
        List<CallLogEntry> logs = new ArrayList<CallLogEntry>();
        logs.add(new CallLogEntry("0000000000", 10));
        logs.add(new CallLogEntry("1111111111", 11));
        logs.add(new CallLogEntry("2222222222", 12));
        logs.add(new CallLogEntry("3333333333", 13));

        boolean result = callLogUtil.containsLogFromNumber(logs, "3333333333");
        assertTrue(result);

        result = callLogUtil.containsLogFromNumber(logs, "4444444444");
        assertFalse(result);
    }
}
