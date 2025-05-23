package com.alerts.AlertFactories;

import com.alerts.Alert;
import com.alerts.BasisAlert;

/**
 * base alert factory class that creates a basis alert
 */
public class AlertFactory {
    /**
     * creates a basis alert instance
     * @param patientId id of patient
     * @param condition condition string (not used)
     * @param timestamp time of alert
     * @return a new basis alert with condition "triggered alert"
     */
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BasisAlert(patientId, "triggered alert", timestamp);
    }
}





