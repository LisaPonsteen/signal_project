package com.alerts.AlertStrategies;

public class DiabolicBloodPressureStrategy extends SystolicBloodPressureStrategy {
    public DiabolicBloodPressureStrategy() {
        upperThreshold = 120;
        lowerThreshold = 60;
    }
}
