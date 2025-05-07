package com.alerts.Decorators;

import com.alerts.Alert;

public class PriorityAlertDecorator extends AlertDecorator{
    public PriorityAlertDecorator(Alert alert) {
        super(alert);
    }

    @Override
    public String toString() {
        return "PRIORITY Alert: [" + super.toString() + "]";
    }
}

