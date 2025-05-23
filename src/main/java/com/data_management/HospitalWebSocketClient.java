package com.data_management;

import java.net.URI;
import java.nio.ByteBuffer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class HospitalWebSocketClient extends WebSocketClient {
    DataStorage dataStorage;

    public HospitalWebSocketClient(URI serverURI, DataStorage dataStorage) {
        super(serverURI);
        this.dataStorage = dataStorage;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("new connection to websocket");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed with exit code " + code + " additional info: " + reason);
    }

    /**
     * When a new message is received, this message will pass it to the data parser.
     * The data parser will update the dataStorage
     *
     * @param message   the new message with patient data
     */
    @Override
    public void onMessage(String message) {
        System.out.println("received message: " + message);
        DataParser.parseLine(message, dataStorage);
    }

    @Override
    public void onMessage(ByteBuffer message) {
        System.out.println("received ByteBuffer");
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("an error occurred:" + ex);
    }
}
