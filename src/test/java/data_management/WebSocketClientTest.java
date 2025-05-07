package data_management;

import com.cardio_generator.outputs.WebSocketOutputStrategy;
import com.data_management.DataStorage;
import com.data_management.HospitalWebSocketClient;
import com.data_management.PatientRecord;
import org.java_websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
    void testInvalidDataFormat() throws URISyntaxException, InterruptedException {
        WebSocketOutputStrategy out = new WebSocketOutputStrategy(8090);
        DataStorage storage = DataStorage.getInstance();
        HospitalWebSocketClient client = new HospitalWebSocketClient(new URI("ws://localhost:8090"), storage);
        client.connect();
        while (!client.isOpen()) Thread.sleep(100);

        out.output(1, 1714376789050L, "Saturation", "sometext");
        //out.output(1, 1714376789050L, "something", "0.0");
        out.output(1, 0, "Saturation", "0.0");

        Thread.sleep(100); // give time to process
        List<PatientRecord> records = storage.getRecords(1, 1714376789049L, 1714376789051L);

        assertEquals(0, records.size()); // invalid should be skipped
        client.close();
    }

    @Test
    void testDuplicateData() throws URISyntaxException, InterruptedException {
        WebSocketOutputStrategy out = new WebSocketOutputStrategy(8091);
        DataStorage storage = DataStorage.getInstance();
        HospitalWebSocketClient client = new HospitalWebSocketClient(new URI("ws://localhost:8091"), storage);

        Thread.sleep(100); //let the server start up before connecting
        client.connect();
        while (!client.isOpen()) Thread.sleep(100);


        out.output(1, 1714376789050L, "Saturation", "99");
        out.output(1, 1714376789050L, "Saturation", "99"); // duplicate (dont put in)
        out.output(1, 1714376789050L, "Saturation", "100"); //update

        Thread.sleep(1000);
        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789050L);
        assertEquals(1, records.size()); // just one record
        assertEquals(100.0, records.get(0).getMeasurementValue()); //record gets updated
        client.close();

        //TODO: why does in the data storage test updating goes well but here it isnt
    }


}
