package com.alerts.AlertStrategies;

/**
 * alert strategy for diabolic blood pressures.
 * Since it's the exact same logic as SystolicBloodPressureStrategy but with different thresholds
 * I extended the class and changed the thresholds
 */
public class DiabolicBloodPressureStrategy extends SystolicBloodPressureStrategy {
    public DiabolicBloodPressureStrategy() {
        upperThreshold = 120;
        lowerThreshold = 60;
    }
}
