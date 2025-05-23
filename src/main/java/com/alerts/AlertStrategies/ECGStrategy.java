package com.alerts.AlertStrategies;

import java.util.LinkedList;
import java.util.Queue;

public class ECGStrategy implements AlertStrategy {
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
            }
        } //I assume we don't have to do checks if the window isnt full yet
        lastECGs.offer(value); //enqueue
        return false;
    }
}
