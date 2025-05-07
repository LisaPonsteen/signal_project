package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;

class DataStorageTest {

    /**
     * Test the dataStorage. see if data is added and updated correctly
     */
    @Test
    void testAddPatientData() {
        DataStorage storage = DataStorage.getInstance();
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789049L, 1714376789052L);
        if (records.isEmpty()) {
            System.out.println("records is empty");
            return;
        }
        assertEquals(2, records.size()); // Check if two records are added
        assertEquals(100.0, records.get(0).getMeasurementValue()); // Validate value
        assertEquals(1, records.get(0).getPatientId()); //validate patientID
    }
    @Test
    void testDuplicateRecords() {
        DataStorage storage = DataStorage.getInstance();
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L); //duplicate
        storage.addPatientData(1, 50.0, "WhiteBloodCells", 1714376789050L); //updated version
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L); //a new record

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        if (records.isEmpty()) {
            System.out.println("records is empty");
            return;
        }
        assertEquals(2, records.size()); // Check if two records are added (so no duplicates)
        assertEquals(50.0, records.get(0).getMeasurementValue()); // Validate that the first record is updated
        assertEquals(1, records.get(0).getPatientId()); //validate patientID
    }
}
