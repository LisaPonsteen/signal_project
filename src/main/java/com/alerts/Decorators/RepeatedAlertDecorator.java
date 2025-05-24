package com.alerts.Decorators;

import com.alerts.Alert;
import com.alerts.AlertStrategies.*;

/**
 * A decorator for alert objects that should be repeated by re-checking alert conditions
 * Each instance holds a reference to the original alert and the {@code AlertStrategy}
 *  and {@code recordType} that made the alert, so it can easily be re-checked
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

