package com.alerts.Decorators;

import com.alerts.Alert;

public class AlertDecorator implements Alert {
    Alert alert;
    public AlertDecorator(Alert alert) {
        this.alert = alert;
    }

    @Override
    public String getPatientId() {
        return alert.getPatientId();
    }

    @Override
    public String getCondition() {
        return alert.getCondition();
    }

    @Override
    public long getTimestamp() {
        return alert.getTimestamp();
    }
    @Override
    public String toString(){
        return alert.toString();
    }
}


