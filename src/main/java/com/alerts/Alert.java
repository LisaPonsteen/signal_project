package com.alerts;

public interface Alert {
    public String getPatientId();
    public String getCondition();
    public long getTimestamp();
    public String toString();
}
// Represents an alert
class BasisAlert implements Alert {
    private String patientId;
    private String condition;
    private long timestamp;

    public BasisAlert(String patientId, String condition, long timestamp) {
        this.patientId = patientId;
        this.condition = condition;
        this.timestamp = timestamp;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getCondition() {
        return condition;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "patientId=" + patientId + ", condition=" + condition + ", timestamp=" + timestamp;
    }
}