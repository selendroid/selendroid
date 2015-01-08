package io.selendroid.server.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

public class CallLogEntry {
    
    // define a date format to parse JSON
    private static final DateFormat dateFormat;
    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));        
    }
    
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
    
    public static CallLogEntry fromJson(String jsonString) {
        
        try {
            JSONObject json = new JSONObject(jsonString);
            CallLogEntry entry = new CallLogEntry(json.optString("number"), json.optInt("duration"));
            entry.direction = json.optInt("direction", INCOMING_TYPE);
            entry.time = dateFormat.parse(json.optString("time"));
            return entry;
        } catch(ParseException e) {
            throw new IllegalArgumentException("Unable to parse CallLogEntry from string " + jsonString, e);
            
        } catch(JSONException e) {
            throw new IllegalArgumentException("Unable to parse CallLogEntry from string " + jsonString, e);
        }        
    }
    
    public String toJSON() {
        try {
            JSONObject json = new JSONObject();
            json.put("number", number);
            json.put("duration", duration);
            json.put("direction", direction);
            json.put("time", dateFormat.format(time));
            return json.toString();
        } catch(JSONException e) {
            throw new IllegalStateException("CallLogEntry cannot be converted to JSONObject", e);
        }
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
