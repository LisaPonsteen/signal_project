package com.alerts.AlertStrategies;

/**
 * interface for alert strategies.
 * instances have a method checkAlert() that returns true if an alert should be triggered and false if not
 */
public interface AlertStrategy {
    /**
     * checks if an alert should be triggered because of a new record
     * @param value value of the record
     * @param time  time of the record
     * @return  true = alert, false = no alert
     */
    boolean checkAlert(double value, Long time);
}






