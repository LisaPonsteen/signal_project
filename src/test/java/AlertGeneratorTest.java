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

public class AlertGeneratorTest {

    @Test
    void testMakeAlert() {
        DataStorage dataStorage = DataStorage.getInstance();
        long timestamp = System.currentTimeMillis();

        dataStorage.addPatientData(1, 90, "Saturation", timestamp);

        AlertGenerator alertGenerator = new AlertGenerator(dataStorage);
        alertGenerator.evaluateData(1);

        //check manually if this is true! since I trigger alerts by printing in the terminal so this is the only way to test it
        System.out.println("Alert should be triggered with timestamp: " + timestamp);
    }

    @Test
    void testCombinedAlerts() {
        DataStorage dataStorage = DataStorage.getInstance();

        dataStorage.addPatientData(1, 90, "Saturation", System.currentTimeMillis());
        dataStorage.addPatientData(1, 89, "SystolicPressure", System.currentTimeMillis());

        AlertGenerator alertGenerator = new AlertGenerator(dataStorage);
        alertGenerator.evaluateData(1);

        System.out.println("a CombinedAlert, BloodOxygenAlert and BloodPressureAlert should be triggered for patient 1");
    }


    @Test
    void testAlertDecorators() {
        long timestamp = System.currentTimeMillis();
        Alert alert = new BasisAlert("1", "Saturation=90", timestamp);
        AlertStrategy saturationStrategy = new SaturationStrategy();

        AlertDecorator repeatedAlertDecorator = new RepeatedAlertDecorator(alert, saturationStrategy, "Saturation");
        AlertDecorator priorityAlertDecorator = new PriorityAlertDecorator(alert);
        System.out.println(repeatedAlertDecorator);
        System.out.println(priorityAlertDecorator);

        System.out.println("a repeated alert and priority alert should be triggered for patient 1");

        assertEquals("1", alert.getPatientId());
        assertEquals("1", repeatedAlertDecorator.getPatientId());
    }

    /**
     * tests if the alert decorators are triggered correctly(especially if repeated alert triggering logic is correct)
     * to test it, I put in evaluate data that saturation should make and trigger a priority and repeated alert
     *
     * @throws InterruptedException if thread.sleep() gets interrupted
     */
    @Test
    void triggeringAlertDecorator() throws InterruptedException {
        DataStorage dataStorage = DataStorage.getInstance();
        dataStorage.addPatientData(1, 90, "Saturation", System.currentTimeMillis());
        AlertGenerator alertGenerator = new AlertGenerator(dataStorage);
        alertGenerator.evaluateData(1);

        dataStorage.addPatientData(1, 89, "Saturation", System.currentTimeMillis()); //triggers
        Thread.sleep(3000);
        dataStorage.addPatientData(1, 88, "Saturation", System.currentTimeMillis()); //triggers
        Thread.sleep(3000);
        dataStorage.addPatientData(1, 100, "Saturation", System.currentTimeMillis()); //doesnt trigger
        Thread.sleep(3000);
        System.out.println("a priority alert should be triggered and a repeated alert that is repeated 2 times");
    }
}
