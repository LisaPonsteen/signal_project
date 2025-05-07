package com.alerts;

/**
 * Interface for objects that represent an alert.
 */
public interface Alert {
    public String getPatientId();
    public String getCondition();
    public long getTimestamp();
}
