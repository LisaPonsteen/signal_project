package com.alerts.AlertStrategies;

/**
 * interface for alert strategies.
 * instances have a method checkAlert() that returns true if an alert should be triggered and false if not
 */
public interface AlertStrategy {
    boolean checkAlert(double value, Long time);
}






