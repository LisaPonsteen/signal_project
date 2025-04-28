package com.alerts;

public class AlertFactory {
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BasisAlert(patientId, condition, timestamp);
    }
}

class BloodPressureAlertFactory extends AlertFactory {
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BasisAlert(patientId, "BloodPressureAlert -> " + condition, timestamp);
    }
}

class BloodOxygenAlertFactory extends AlertFactory{
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BasisAlert(patientId, "BloodOxygenAlert -> " + condition, timestamp);
    }
}
class ECGAlertFactory extends AlertFactory{
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BasisAlert(patientId, "ECGAlert -> " + condition, timestamp);
    }
}

class CombinedAlertFactory extends AlertFactory{
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BasisAlert(patientId, "CombinedAlert -> " + condition, timestamp);
    }
}


