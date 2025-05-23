package com.data_management;

import org.java_websocket.client.WebSocketClient;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Implementation of DataReader for websocket inputs.
 * when an instance is initialized it creates a client for the websocket.
 * when that client is connected after calling the method readData for the first time, it will continuously parse and add data to the data storage.
 * it felt like a counterintuitive implementation, but this way I can still let the class implement DataReader. (changing DataReader will lead to it not fitting with FileReader anymore)
 */
public class WebSocketReader implements DataReader {
    WebSocketClient webSocketClient;

    /**
     * Constructor that creates a webSocketClient on port 8080
     *
     * @param dataStorage   the storage where incoming data will be stored
     * @throws URISyntaxException throws an exception when the uri cant be initialized correctly
     */
    public WebSocketReader(DataStorage dataStorage) throws URISyntaxException {
        this.webSocketClient= new HospitalWebSocketClient(new URI("ws://localhost:8080"), dataStorage);
    }

    /**
     * Reads data from a websocket generated using the --output websocket:<port> argument
     * @param dataStorage the storage where data will be stored, but it isn't used in this implementation
     */
    @Override
    public void readData(DataStorage dataStorage){
        try {
            webSocketClient.connect();
        } catch (Exception e) {
            System.out.println("websocket connection failed");
            throw new RuntimeException(e);
        }
    }
}