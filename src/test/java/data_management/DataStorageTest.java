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
        assertEquals(100.0, records.get(0).getMeasurementValue()); // Validate first value
        assertEquals(1, records.get(0).getPatientId()); //validate patientID
    }

    @Test
    void testOrderingOfRecords() throws InterruptedException {
        DataStorage storage = DataStorage.getInstance();
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L); //latest
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L); //first

        Thread.sleep(1000);
        List<PatientRecord> records = storage.getRecords(1, 1714376789049L, 1714376789052L);

        assertEquals(100.0, records.get(0).getMeasurementValue()); //check if the list of records is correctly ordered in based on time
    }

    @Test
    void testGetLastRecordOfType() throws InterruptedException {
        DataStorage storage = DataStorage.getInstance();
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789052L); //latest white bloodcells
        storage.addPatientData(1, 90, "Saturation", 1714376789053L); //latest record
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L); //first white bloodcells

        Thread.sleep(1000);
        List<PatientRecord> records = storage.getRecords(1, 1714376789049L, 1714376789059L);

        PatientRecord record = storage.getLastRecordOfType(1, "WhiteBloodCells"); //should return the last (on timestamp) record of whitebloodcell
        assertEquals(200.0, record.getMeasurementValue());
        assertEquals("WhiteBloodCells", record.getRecordType());
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
