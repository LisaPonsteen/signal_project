package com.alerts.AlertStrategies;

import java.util.LinkedList;
import java.util.Queue;

public class SaturationStrategy implements AlertStrategy {
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
