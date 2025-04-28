package com.alerts;

/**
 * Interface for objects that represent an alert.
 */
public interface Alert {
    public String getPatientId();
    public String getCondition();
    public long getTimestamp();
    public void print();
}

/**
 * basic alert implementation. the alerts have a patientID, condition and timestamp, getters fot those fields and a print() method that prints the information about the alert.
 */
class BasisAlert implements Alert {
    private String patientId;
    private String condition;
    private long timestamp;
    private String conditionType;

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
    public String getConditionType() {
        return conditionType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "patientId=" + patientId + ", condition=" + condition + ", timestamp=";
    }
    @Override
    public void print() {
        System.out.println(this.toString());
    }
}