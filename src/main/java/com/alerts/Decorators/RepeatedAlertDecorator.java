package com.alerts.Decorators;

import com.alerts.Alert;
import com.alerts.AlertStrategy;

/**
 * a decorator for alert objects. It repeats alerts by checking and re-checking alert conditions over a set interval
 */
public class RepeatedAlertDecorator extends AlertDecorator {
    Alert alert;
    AlertStrategy alertStrategy;
    public RepeatedAlertDecorator(Alert alert, AlertStrategy alertStrategy) {
        super(alert);
        this.alertStrategy = alertStrategy;
    }

    @Override
    public String toString() {
        return alert.toString();
    }

    /**
     * method that prints the alert 2 times
     */
    @Override
    public void print(){
        for (int i = 0; i < 3; i++) {
            System.out.println(this.toString() + ", Repeated " + i + " times");
            try {
                Thread.sleep(3000); // 3 seconds between prints
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Interrupted!");
            }
        }
    }
}

