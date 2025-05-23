package data_management;

import static org.junit.jupiter.api.Assertions.*;

import com.data_management.DataParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.net.URISyntaxException;
import java.util.List;

public class DataParserTest {

    @Test
    void testDataParser() throws URISyntaxException {
        DataStorage dataStorage = DataStorage.getInstance();
        DataParser.parseLine("Patient ID: 1, Timestamp: 1714376789049L, Label: ECG, Data: 85", dataStorage);

        List<PatientRecord> records = dataStorage.getRecords(1, 1714376789049L, 1714376789049L);

        Assertions.assertEquals(1, records.size());
        Assertions.assertEquals(1714376789049L, records.get(0).getTimestamp());
        Assertions.assertEquals("ECG", records.get(0).getRecordType());
        Assertions.assertEquals(85, records.get(0).getMeasurementValue());
    }
    @Test
    void testEdgeCases() throws URISyntaxException, InterruptedException {
        DataStorage dataStorage = DataStorage.getInstance();
        DataParser.parseLine("Patient ID: 1, Label: Saturation", dataStorage);
        DataParser.parseLine("Patient ID: abc, Timestamp: xyz, Label: ECG, Data: 75", dataStorage);
        DataParser.parseLine("Patient ID: abc, Timestamp: xyz, Label: ECG, Data: 75", dataStorage);
        DataParser.parseLine("Patient ID: 1, Timestamp: -123456, Label: ECG, Data: -50", dataStorage);
        DataParser.parseLine("Patient ID: 1, Timestamp: 9999999999999L, Label: ECG, Data: 99999", dataStorage);
        DataParser.parseLine("", dataStorage);

        List<PatientRecord> records = dataStorage.getRecords(1, 0, 9999999999999L);

        Assertions.assertEquals(1, records.size());
    }
}
