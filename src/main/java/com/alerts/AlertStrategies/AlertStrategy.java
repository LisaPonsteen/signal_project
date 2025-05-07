package com.alerts.AlertStrategies;

import java.util.LinkedList;
import java.util.Queue;

public interface AlertStrategy {
    public boolean checkAlert(double value, Long time);
}






