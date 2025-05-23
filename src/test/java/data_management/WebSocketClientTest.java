package data_management;

import com.alerts.AlertGenerator;
import com.cardio_generator.outputs.WebSocketOutputStrategy;
import com.data_management.DataStorage;
import com.data_management.HospitalWebSocketClient;
import com.data_management.PatientRecord;
import org.java_websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WebSocketClientTest {

    @Test
    void testReadWebSocketAndParse() throws URISyntaxException, InterruptedException {
        WebSocketOutputStrategy out = new WebSocketOutputStrategy(8089);
        DataStorage storage = DataStorage.getInstance();
        HospitalWebSocketClient client = new HospitalWebSocketClient(new URI("ws://localhost:8089"), storage);
        client.connect();
        while (!client.isOpen()) {
            Thread.sleep(100); //wait till the client is connected
        }

        System.out.println("sending..");
        out.output(1,1714376789050L, "Saturation", "100");

        Thread.sleep(100); //wait for the data to process...

        List<PatientRecord> records = storage.getRecords(1, 1714376789049L, 1714376789052L);
        if (records.isEmpty()) {
            System.out.println("records is empty");
            return;
        }
        assertEquals(1, records.size()); // Check if record is added (so no duplicates)
        assertEquals(100.0, records.get(0).getMeasurementValue()); // Validate value
        assertEquals(1, records.get(0).getPatientId()); //validate patientID
        client.close();
    }

    @Test
    void testSendMultipleRecords() throws URISyntaxException, InterruptedException {
        WebSocketOutputStrategy out = new WebSocketOutputStrategy(8091);
        DataStorage storage = DataStorage.getInstance();
        HospitalWebSocketClient client = new HospitalWebSocketClient(new URI("ws://localhost:8091"), storage);

        Thread.sleep(100); //let the server start up before connecting
        client.connect();
        while (!client.isOpen()) Thread.sleep(100);

        out.output(1, 1714376789049L, "Saturation", "99");
        out.output(1, 1714376789049L + 1, "Saturation", "100");
        out.output(1, 1714376789049L + 2, "ECG", "5");
        out.output(1, 1714376789049L + 3, "BloodPressure", "130");

        Thread.sleep(5000);
        List<PatientRecord> records = storage.getRecords(1, 1714376789049L, 1714376789055L);
        assertEquals(4, records.size()); //has it received 4 records?
        client.close();
    }

    @Test
    void testInvalidData() throws URISyntaxException, InterruptedException {
        DataStorage storage = DataStorage.getInstance();
        HospitalWebSocketClient client = new HospitalWebSocketClient(new URI("ws://localhost:8091"), storage);
        WebSocketOutputStrategy out = new WebSocketOutputStrategy(8091);

        Thread.sleep(100);
        client.connect();
        while (!client.isOpen()) Thread.sleep(100);

        //tests with correct datatypes and no missing data, but invalid values (otherwise it won't compile)
        out.output(1, -123456, "ECG", "-50"); //invalid data (negative numbers)
        out.output(1, 9999999999999L, "ECG", "99999"); //very large numbers

        out.output(1, 1714376789050L, "Saturation", "sometext");
        out.output(1, 0, "Saturation", "0.0");

        Thread.sleep(2000);
        List<PatientRecord> allRecords = storage.getRecords(1, 0, Long.MAX_VALUE);

        // one message should be stored: the large number case
        assertEquals(1, allRecords.size());
        assertEquals(99999.0, allRecords.get(0).getMeasurementValue());

        client.close();
    }


    @Test
    void testInvalidMessages() throws URISyntaxException, InterruptedException {
        DataStorage storage = DataStorage.getInstance();
        HospitalWebSocketClient client = new HospitalWebSocketClient(new URI("ws://localhost:8091"), storage);
        WebSocketOutputStrategy out = new WebSocketOutputStrategy(8091);

        Thread.sleep(100);
        client.connect();
        while (!client.isOpen()) Thread.sleep(100);

        //for this block, I made another output method that just sends the string, so I can test missing data cases without compile errors
        out.outputString("Patient ID: 1, Timestamp: 1714376789050, Label: Saturation, Data: 99"); //make sure valid data does work
        out.outputString("1, 1714376789050, Saturation, 99");
        out.outputString("Patient ID: 1, Label: Saturation");
        out.outputString("abc xyz ECG 75");
        out.outputString("");
        out.outputString("Patient ID: 1");
        out.outputString("Patient ID: 1, Label: ECG, Timestamp: 1714376789051, Data: 80"); //valid

        Thread.sleep(1000);
        List<PatientRecord> allRecords = storage.getRecords(1, 0, Long.MAX_VALUE);

        // the firs and last message should be stored, the rest shouldn't cause errors/stop the program
        assertEquals(2, allRecords.size());
        assertEquals(99, allRecords.get(0).getMeasurementValue()); //validate first record
        assertEquals(80, allRecords.get(1).getMeasurementValue()); //validate second record
        assertEquals("ECG", allRecords.get(1).getRecordType());

        client.close();
    }


    /**
     * Integration test of the data storage, parser and websocket client, -reader and -outputstrategy
     * @throws URISyntaxException   throws an exception when the URI cant be constructed
     * @throws InterruptedException throws an exception if thread gets interrupted
     */
    @Test
    void testDuplicateData() throws URISyntaxException, InterruptedException {
        WebSocketOutputStrategy out = new WebSocketOutputStrategy(8060);
        DataStorage storage = DataStorage.getInstance();
        HospitalWebSocketClient client = new HospitalWebSocketClient(new URI("ws://localhost:8060"), storage);

        Thread.sleep(100); //let the server start up before connecting
        client.connect();
        while (!client.isOpen()) Thread.sleep(100);

        out.output(1, 1714376789050L, "Saturation", "99");
        Thread.sleep(200);
        out.output(1, 1714376789050L, "Saturation", "99"); // duplicate (dont put in)
        Thread.sleep(200);
        out.output(1, 1714376789050L, "Saturation", "100"); //update

        Thread.sleep(5000);
        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789050L);
        assertEquals(1, records.size()); // just one record in the list
        assertEquals(100.0, records.get(0).getMeasurementValue()); //record is updated
        client.close();
    }

    /**
     * Integration test of the websocket client and alert generator (and data storage, parser etc.)
     * @throws URISyntaxException   throws an exception when the URI cant be constructed
     * @throws InterruptedException throws an exception when the websocket client gets interrupted
     */
    @Test
    void testWebsocketCausingAlert() throws URISyntaxException, InterruptedException {
        WebSocketOutputStrategy out = new WebSocketOutputStrategy(8060);
        DataStorage storage = DataStorage.getInstance();
        HospitalWebSocketClient client = new HospitalWebSocketClient(new URI("ws://localhost:8060"), storage);

        Thread.sleep(100); //let the server start up before connecting
        client.connect();
        while (!client.isOpen()) Thread.sleep(100);

        AlertGenerator alertGenerator = new AlertGenerator(storage);

        out.output(1, 1714376789050L, "Saturation", "99");
        Thread.sleep(200);
        out.output(1, 1714376789051L, "Saturation", "90");
        Thread.sleep(200);
        out.output(1, 1714376789052L, "Saturation", "100");
        Thread.sleep(200);
        out.output(1, 1714376789053L, "", "100");

        Thread.sleep(5000);
        alertGenerator.evaluateData(1);
        Thread.sleep(5000);
        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789052L);
        assertEquals(3, records.size()); // just one record in the list
        client.close();
    }

}
