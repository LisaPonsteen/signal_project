package com.alerts.AlertStrategies;

public class CombinedAlertStrategy implements AlertStrategy {
    private double lastSaturation = 100;
    private double lastSystolicPressure = 100;

    public void setLastData(double value, String recordType) {
        if (recordType.equals("Saturation")) {
            lastSaturation = value;
        } else if (recordType.equals("SystolicPressure")) {
            lastSystolicPressure = value;
        } else {
            System.out.println("Unknown record type for updating combined alert strategy: " + recordType);
        }
    }

    @Override
    public boolean checkAlert(double value, Long time) {
        return lastSystolicPressure < 90 && lastSaturation < 92;
    }
}

