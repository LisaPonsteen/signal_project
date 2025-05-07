package com.alerts.AlertFactories;

import com.alerts.Alert;
import com.alerts.BasisAlert;

public class BloodPressureAlertFactory extends AlertFactory {
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BasisAlert(patientId, "BloodPressureAlert -> " + condition, timestamp);
    }
}