package com.alerts.AlertFactories;

import com.alerts.Alert;
import com.alerts.BasisAlert;

/**
 * alert factory for alerts based on combined data (e.g. saturation and blood pressure)
 */
public class CombinedAlertFactory extends AlertFactory{
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BasisAlert(patientId, "CombinedAlert -> " + condition, timestamp);
    }
}