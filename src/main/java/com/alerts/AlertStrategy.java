package com.alerts;

import java.util.LinkedList;
import java.util.Queue;

public interface AlertStrategy {
    public boolean checkAlert(double value, long time);
}

class CombinedAlertStrategy implements AlertStrategy {
    double lastSaturation = 100;
    double lastSystolicPressure = 100;

    public void setLastSaturation(double lastSaturation) {
        this.lastSaturation = lastSaturation;
    }
    public void setLastSystolicPressure(double lastSystolicPressure) {
        this.lastSystolicPressure = lastSystolicPressure;
    }

    @Override
    public boolean checkAlert(double value, long time) {
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
    int trend = 0;
    boolean increase = false;
    boolean decrease = false;
    boolean firstRecord = true;
    double lastPressure;
    int trendThreshold = 10;
    int upperThreshold;
    int lowerThreshold;
    public SystolicBloodPressureStrategy() {
        upperThreshold = 180;
        lowerThreshold = 90;
    }
    @Override
    public boolean checkAlert(double value, long time) {
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
        if (trend >= 3) {
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
    Queue<Double> lastECGs = new LinkedList<>();
    int ECGSize = 5;
    int ecgPeakThreshold = 30;
    double averageECG = 0;

    @Override
    public boolean checkAlert(double value, long time) {
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
    Queue<Double> lastSaturations = new LinkedList<>();
    Queue<Long> lastTimes = new LinkedList<>();
    long lastTime;

    @Override
    public boolean checkAlert(double value, long time) {
        lastSaturations.offer(value);
        lastTimes.offer(lastTime);

        if (lastTime - lastTimes.peek() > 600000) { //assume time is in miliseconds, since that is in discription of the generator
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




