package com.alerts.AlertFactories;

import com.alerts.Alert;
import com.alerts.BasisAlert;

/**
 * alert factory for alerts based on saturation levels
 */
public class BloodOxygenAlertFactory extends AlertFactory{
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BasisAlert(patientId, "BloodOxygenAlert -> " + condition, timestamp);
    }
}

