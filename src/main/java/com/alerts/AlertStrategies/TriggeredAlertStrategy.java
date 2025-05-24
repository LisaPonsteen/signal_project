package com.alerts.AlertStrategies;

public class TriggeredAlertStrategy implements AlertStrategy {
    @Override
    public boolean checkAlert(double value, Long time) {
        return true;
        //always should return true, because the dataParser only passes on triggered alerts and not resolved ones
    }
}
