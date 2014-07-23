package io.selendroid.server.utils;

import java.util.Date;

public class CallLogEntry {
    public String number;
    public int duration;
    public Date time;
    public int direction;
    public static final int INCOMING_TYPE = 1;
    public static final int OUTGOING_TYPE = 2;
    public static final int MISSED_TYPE = 3;
    
    public CallLogEntry(String number, int duration) {
        this(number, duration, new Date(), INCOMING_TYPE);
    }
    
    /**
     * Number of caller.
     * @param number
     * Duration of call.
     * @param duration
     * Time as a date object of call.
     * @param time
     * Direction of call, use INCOMING_TYPE, OUTGOING_TYPE, or MISSED_TYPE
     * @param direction
     */
    public CallLogEntry(String number, int duration, Date time, int direction) {
        this.number = number;
        this.duration = duration;
        this.time = time;
        this.direction = direction;
    }
    
    public String getNumber() {
        return number;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public Date getDate() {
        return time;
    }
    
    public int getDirection() {
        return direction;
    }
    
    
}
