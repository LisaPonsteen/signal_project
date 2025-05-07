package com.alerts;

/**
 * basic alert implementation. the alerts have a patientID, condition and timestamp, getters fot those fields and a print() method that prints the information about the alert.
 */
public class BasisAlert implements Alert {
    private String patientId;
    private String condition;
    private long timestamp;

    public BasisAlert(String patientId, String condition, long timestamp) {
        this.patientId = patientId;
        this.condition = condition;
        this.timestamp = timestamp;
    }

    @Override
    public String getPatientId() {
        return patientId;
    }

    @Override
    public String getCondition() {
        return condition;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "patientId=" + patientId + ", condition:" + condition + ", timestamp=" + timestamp;
    }
}