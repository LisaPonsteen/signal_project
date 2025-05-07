package com.alerts.Decorators;

import com.alerts.Alert;
import com.alerts.AlertStrategies.*;

/**
 * a decorator for alert objects. It repeats alerts by checking and re-checking alert conditions over a set interval
 */
public class RepeatedAlertDecorator extends AlertDecorator {
    Alert alert;
    AlertStrategy alertStrategy;
    String recordType;
    public RepeatedAlertDecorator(Alert alert, AlertStrategy alertStrategy, String recordType) {
        super(alert);
        this.alertStrategy = alertStrategy;
        this.recordType = recordType;
    }
    @Override
    public String toString() {
        return "REPEATED Alert: [" + super.toString() + " ]";
    }

    public AlertStrategy getAlertStrategy() {
        return alertStrategy;
    }
    public String getRecordType() {
        return recordType;
    }
}

