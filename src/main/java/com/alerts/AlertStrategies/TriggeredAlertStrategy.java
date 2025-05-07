package com.alerts.AlertStrategies;

public class TriggeredAlertStrategy implements AlertStrategy {
    @Override
    public boolean checkAlert(double value, Long time) {
        return true;
    }
}
