package com.alerts.AlertFactories;

import com.alerts.Alert;
import com.alerts.BasisAlert;

public class AlertFactory {
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BasisAlert(patientId, "triggered alert", timestamp);
    }
}





