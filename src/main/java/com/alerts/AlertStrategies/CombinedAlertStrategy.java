package com.alerts.AlertStrategies;

import com.alerts.RecordType;

public class CombinedAlertStrategy implements AlertStrategy {
    private double lastSaturation = 100;
    private double lastSystolicPressure = 100;

    /**
     * method that updates either the latest saturation or systolic pressure
     * this method is needed because in check alert, we don't know of which type the new data is
     * @param value new value
     * @param recordType    record type of value. either saturation or systolic
     */
    public void setLastData(double value, String recordType) {
        if (recordType.equals(RecordType.SATURATION)) {
            lastSaturation = value;
        } else if (recordType.equals(RecordType.SYSTOLIC)) {
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

