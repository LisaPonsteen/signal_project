package data_management;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.AlertGenerator;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;

import com.data_management.Patient;

public class AlertGeneratorTest {

    @Test
    void testMakeAlert() {
        DataStorage dataStorage = new DataStorage();
        long timestamp = System.currentTimeMillis();

        dataStorage.addPatientData(1, 90, "Saturation", timestamp);

        AlertGenerator alertGenerator = new AlertGenerator(dataStorage);
        alertGenerator.evaluateData(1);

        System.out.println("Alert should be triggered with timestamp: " + timestamp);
    }
}
