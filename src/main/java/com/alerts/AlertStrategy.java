package com.alerts;

import java.util.LinkedList;
import java.util.Queue;

public interface AlertStrategy {
    public boolean checkAlert(double value, Long time);
}

class CombinedAlertStrategy implements AlertStrategy {
    private double lastSaturation = 100;
    private double lastSystolicPressure = 100;

    public void setLastSaturation(double lastSaturation) {
        this.lastSaturation = lastSaturation;
    }
    public void setLastSystolicPressure(double lastSystolicPressure) {
        this.lastSystolicPressure = lastSystolicPressure;
    }

    @Override
    public boolean checkAlert(double value, Long time) {
        if (lastSystolicPressure < 90 && lastSaturation < 92) {
            return true;
            //triggerAlert(new Alert(String.valueOf(patientId), "hypotensiveHypoxemia", lastTime));
        }
        return false;
    }
}

class DiabolicBloodPressureStrategy extends SystolicBloodPressureStrategy {
    public DiabolicBloodPressureStrategy() {
        upperThreshold = 120;
        lowerThreshold = 60;
    }
}


class SystolicBloodPressureStrategy implements AlertStrategy {
    private int trend = 0;
    private boolean increase = false;
    private boolean decrease = false;
    private boolean firstRecord = true;
    private double lastPressure;
    private final int trendThreshold = 10;
    int upperThreshold;
    int lowerThreshold;
    int repetitionsForTrend = 3;
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
        if (trend >= repetitionsForTrend) {
            //triggerAlert(new Alert(String.valueOf(patientId), "trend", lastTime));
            return true;
        }
        if (value > upperThreshold || value < lowerThreshold) {
            return true;
            //triggerAlert(new Alert(String.valueOf(patientId), "CriticalThreshold", lastTime));
        }
        return false;
    }
}

class ECGStrategy implements AlertStrategy {
    private Queue<Double> lastECGs = new LinkedList<>();
    private final int ECGSize = 5;
    private final int ecgPeakThreshold = 30;
    private double averageECG = 0;

    public int getECGSize() {
        return ECGSize;
    }

    @Override
    public boolean checkAlert(double value, Long time) {
        if (ECGSize == lastECGs.size()) {
            //update average and dequeue
            averageECG += value/ECGSize;
            averageECG -= lastECGs.poll();
            if (Math.abs(value - averageECG) > ecgPeakThreshold) {
                lastECGs.offer(value); //enqueue
                return true;
                //triggerAlert(new Alert(String.valueOf(patientId), "Abnormal ECG Data", lastTime));
            }
        } //I assume we don't have to check things before the window gets full
        lastECGs.offer(value); //enqueue
        return false;
    }
}

class SaturationStrategy implements AlertStrategy {
    private Queue<Double> lastSaturations;
    private Queue<Long> lastTimes;
    private long lastTime;
    private long timeWindow = 600000; //10 minutes. assuming time is in miliseconds, since that is in discription of the generator

    public long getWindowSize() {
        return timeWindow;
    }

    //Stack<Long> lastTimes, Stack<Double>lastSaturations, long lastTime
    public SaturationStrategy() {
        this.lastSaturations = new LinkedList<>();
        this.lastTimes = new LinkedList<>();
        this.lastTime = lastTime;

    }

    @Override
    public boolean checkAlert(double value, Long time) {
        lastSaturations.offer(value);
        lastTimes.offer(lastTime);

        if (lastTime - lastTimes.peek() > timeWindow) {
            //update the windows
            lastTimes.poll();
            lastSaturations.poll();
        }
        if (value < 92) {
            return true;
            //triggerAlert(new Alert(String.valueOf(patientId), "Low Saturation", lastTime));
        }
        if (lastSaturations.peek() - value > 5) {
            return true;
            //triggerAlert(new Alert(String.valueOf(patientId), "Rapid Drop", lastTime));
        }
        return false;
    }
}




