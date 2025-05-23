package com.alerts;

/**
 * Interface for objects that represent an alert.
 */
public interface Alert {
    String getPatientId();
    String getCondition();
    long getTimestamp();
}
