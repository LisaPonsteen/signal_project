package com.alerts.AlertStrategies;

public class SystolicBloodPressureStrategy implements AlertStrategy {
    private int trend = 0;
    private boolean increase = false;
    private boolean decrease = false;
    private boolean firstRecord = true;
    private double lastPressure;
    private final int trendThreshold = 10;
    private final int repetitionsForTrend = 3;

    int upperThreshold;
    int lowerThreshold;
    public SystolicBloodPressureStrategy() {
        upperThreshold = 180;
        lowerThreshold = 90;
    }

    public int getRepetitionsForTrend(){
        return repetitionsForTrend;
    }

    @Override
    public boolean checkAlert(double value, Long time) {
        if (firstRecord) {
            firstRecord = false;
        } else if (value - lastPressure > trendThreshold) {
            if (!increase) {
                trend = 0;
                increase = true;
                decrease = false;
            }
            trend++;
        } else if (lastPressure - value > trendThreshold) {
            if (!decrease) {
                trend = 0;
                increase = false;
                decrease = true;
            }
            trend++;
        } else {
            increase = false;
            decrease = false;
            trend = 0;
        }
        lastPressure = value;
        if (trend >= repetitionsForTrend)
            return true;
        if (value > upperThreshold || value < lowerThreshold)
            return true;
        return false;
    }
}

