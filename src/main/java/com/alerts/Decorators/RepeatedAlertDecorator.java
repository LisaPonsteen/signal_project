package com.alerts.Decorators;

import com.alerts.Alert;

public class RepeatedAlertDecorator extends AlertDecorator {
    Alert alert;
    public RepeatedAlertDecorator(Alert alert) {
        super(alert);
    }

    @Override
    public String toString() {
        return alert.getPatientId() + ", repeated alert";
    }
}

