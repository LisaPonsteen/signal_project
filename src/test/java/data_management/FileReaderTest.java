package data_management;

import static org.junit.jupiter.api.Assertions.*;

import com.data_management.FileReader;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.io.*;
import java.util.List;

public class FileReaderTest {

    /**
     * tests the file reader class and the integration between filereader and datastorage
     * @throws IOException if writing in/making files and directories goes wrong
     */
    @Test
    void testReadFileAndParse() throws IOException {
        String outputDir = "/Users/lisa/Documents/semester2dsai/testDir";
        FileReader reader = new FileReader(outputDir);
        DataStorage storage = DataStorage.getInstance();

        File dir = new File(outputDir);
        File testData1 = new File(outputDir + "/data1.txt");

        if (!dir.exists()) {
            if (!dir.mkdir()) {
                System.out.println("couldn't create directory");
                return;
            }
        }
        if (!testData1.exists()) {
            if (!testData1.createNewFile()) {
                System.out.println("couldn't create file");
                return;
            }
            FileWriter writer = new FileWriter(testData1);
            writer.write("Patient ID: 1, Timestamp: 1714376789052L, Label: WhiteBloodCells, Data: 101\n");
            writer.write("Patient ID: 1, Timestamp: 1714376789050L, Label: WhiteBloodCells, Data: 100\n");
            writer.write("Patient ID: 1, Timestamp: 1714376789051L, Label: alert, Data: triggered\n");
            writer.write("Patient ID: 1, Timestamp: 1714376789051L, Label: alert, Data: resolved\n");
            writer.close();
        }

        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789052L);

        if (records.isEmpty()) {
            System.out.println("records is empty");
            return;
        }
        assertEquals(3, records.size()); // Check if 3 records are retrieved (so the white blood cells and triggered alert, and not the resolved alert)
        assertEquals(100.0, records.get(0).getMeasurementValue()); // Validate first record
        assertEquals(101.0, records.get(2).getMeasurementValue()); // Validate that the 3rd record is actually 3rd in the list (so list is ordered wright)
        assertEquals("alert", records.get(1).getRecordType()); //validate second record (the triggered alert)

    }

    @Test
    void testEdgeCase() throws IOException {
        String outputDir = "/Users/lisa/Documents/semester2dsai/testDir";
        FileReader reader = new FileReader(outputDir);
        DataStorage storage = DataStorage.getInstance();

        File dir = new File(outputDir);
        File testData1 = new File(outputDir + "/data1.txt");

        if (!dir.exists()) {
            if (!dir.mkdir()) {
                System.out.println("couldn't create directory");
                return;
            }
        }
        if (!testData1.exists()) {
            if (!testData1.createNewFile()) {
                System.out.println("couldn't create file");
                return;
            }
            FileWriter writer = new FileWriter(testData1);
            writer.write("Patient ID: 1, Timestamp: -1L, Label: WhiteBloodCells, Data: 101\n"); //negative time
            writer.write("Patient ID: 1, Timestamp: 1714376789050L, Label: WhiteBloodCells, Data: 100"); //no \n
            writer.write("patient 1, 100 bpm\n"); //invalid form
            writer.write("Patient ID: 1, Timestamp: 1714376789050L\n"); //missing data
            writer.write("Patient ID: 1, Timestamp: 1714376789051L, Label: alert, Data: triggered\n"); //valid record
            writer.write("Patient ID: 1, Timestamp: 1714376789052L, Label: WhiteBloodCells, Data: 101\n"); //valid
            writer.close();
        }

        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789052L);

        if (records.isEmpty()) {
            System.out.println("records is empty");
            return;
        }
        assertEquals(2, records.size()); // Check if the last 2 valid records are received
        assertEquals(1, records.get(0).getPatientId()); // Validate record
    }
}
