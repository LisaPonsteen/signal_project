package data_management;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.alerts.AlertStrategies.AlertStrategy;
import com.alerts.AlertStrategies.SaturationStrategy;
import com.alerts.BasisAlert;
import com.alerts.Decorators.AlertDecorator;
import com.alerts.Decorators.PriorityAlertDecorator;
import com.alerts.Decorators.RepeatedAlertDecorator;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;

import com.data_management.Patient;

public class AlertGeneratorTest {

    @Test
    void testMakeAlert() {
        DataStorage dataStorage = DataStorage.getInstance();
        long timestamp = System.currentTimeMillis();

        dataStorage.addPatientData(1, 90, "Saturation", timestamp);

        AlertGenerator alertGenerator = new AlertGenerator(dataStorage);
        alertGenerator.evaluateData(1);

        System.out.println("Alert should be triggered with timestamp: " + timestamp);
    }

    @Test
    void testCombinedAlerts() {
        DataStorage dataStorage = DataStorage.getInstance();

        dataStorage.addPatientData(1, 90, "Saturation", System.currentTimeMillis());
        dataStorage.addPatientData(1, 89, "SystolicPressure", System.currentTimeMillis());

        AlertGenerator alertGenerator = new AlertGenerator(dataStorage);
        alertGenerator.evaluateData(1);

        System.out.println("a CombinedAlert, BloodOxygenAlert and BloodpressureAlert should be triggered");
    }

    @Test
    void testAlertDecorators() {
        DataStorage dataStorage = DataStorage.getInstance();
        long timestamp = System.currentTimeMillis();

        dataStorage.addPatientData(1, 90, "Saturation", timestamp);

        AlertGenerator alertGenerator = new AlertGenerator(dataStorage);
        Alert alert = new BasisAlert("1", "Saturation=90", timestamp);
        AlertStrategy saturationStrategy = new SaturationStrategy();

        assertEquals("1", alert.getPatientId());

        AlertDecorator repeatedAlertDecorator = new RepeatedAlertDecorator(alert, saturationStrategy, "Saturation");
        AlertDecorator priorityAlertDecorator = new PriorityAlertDecorator(alert);

        assertEquals("1", repeatedAlertDecorator.getPatientId());

        System.out.println(repeatedAlertDecorator);
        System.out.println(priorityAlertDecorator);

        //alertGenerator.triggerAlert() is private, so how can I test it if the repeated alert gets triggered correctly?

        //alertGenerator.evaluateData(1); //this doesnt make any alertDecorators
    }
}
