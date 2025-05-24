package com.alerts.AlertFactories;

import com.alerts.Alert;
import com.alerts.BasisAlert;

/**
 * alert factory for alerts based on ecg readings
 */
public class ECGAlertFactory extends AlertFactory{
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BasisAlert(patientId, "ECGAlert -> " + condition, timestamp);
    }
}
