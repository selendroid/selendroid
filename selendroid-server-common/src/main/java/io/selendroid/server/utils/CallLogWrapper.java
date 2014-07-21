package io.selendroid.server.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallLogWrapper {

	//list of calls made. include name and duration.
	
	List<SingleCallLog> logs;
	
	public CallLogWrapper() {
		logs = new ArrayList<SingleCallLog>();
	}
	
	public void addLog(String number, int duration, Date date, int direction) {
		logs.add(new SingleCallLog(number,duration,date,direction));
	}
	
	/**
	 * Returns logs in the format 
	 * Caller Number: 5555555555
	 * Call Duration: 100
	 * --------------------------
	 */
	public List<SingleCallLog> getAllLogs() {
		return logs;
	}
	
	/**
	 * duration specifies call duration to test against.
	 * @param duration
	 * greaterthan set to true finds call durations greater than or equal to specified. false 
	 * finds those less than.
	 * @param greaterthan
	 * @return
	 */
	public List<SingleCallLog> getAllLogsOfDuration(int duration, boolean greaterthan) {
		List<SingleCallLog> list = new ArrayList<SingleCallLog>();
		for(SingleCallLog log : logs) {
			if(log.duration<duration ^ greaterthan) {
			    list.add(log);
			}
		}
		return list;
	}
	
	/**
	 * returns true if call log of specified number exists
	 */
	public boolean containsLogFromNumber(String number) {
        for(SingleCallLog log : logs) {
            if(log.number.equals(number)) {
                return true;
            }
        }
        return false;
	}

}
