package com.alerts.AlertStrategies;

public class TriggeredAlertStrategy implements AlertStrategy {
    @Override
    public boolean checkAlert(double value, Long time) {
        return true; //always return true, because the data parser already only passes on triggered alerts and not resolved ones
    }
}
