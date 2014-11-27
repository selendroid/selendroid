package io.selendroid.server.common.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallLogUtil {

	/**
	 * duration specifies call duration to test against.
	 * @param duration
	 * greaterthan set to true finds call durations greater than or equal to specified. false 
	 * finds those less than.
	 * @param greaterthan
	 * @return
	 */
	public static List<CallLogEntry> getAllLogsOfDuration(List<CallLogEntry> logs, int duration, boolean greaterthan) {
		List<CallLogEntry> list = new ArrayList<CallLogEntry>();
		for(CallLogEntry log : logs) {
			if(log.duration<duration ^ greaterthan) {
			    list.add(log);
			}
		}
		return list;
	}
	
	/**
	 * returns true if call log of specified number exists
	 */
	public static boolean containsLogFromNumber(List<CallLogEntry> logs, String number) {
        for(CallLogEntry log : logs) {
            if(log.number.equals(number)) {
                return true;
            }
        }
        return false;
	}

}
