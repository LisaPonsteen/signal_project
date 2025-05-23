package com.cardio_generator.outputs;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;

public class WebSocketOutputStrategy implements OutputStrategy {

    private WebSocketServer server;

    public WebSocketOutputStrategy(int port) {
        server = new SimpleWebSocketServer(new InetSocketAddress(port));
        System.out.println("WebSocket server created on port: " + port + ", listening for connections...");
        server.start();
    }

    /**
     * Outputs patient data to a websocket.
     * Assumes that lines should look like (in example): Patient ID: 123, Timestamp: 1713345600000, Label: Saturation, Data: 78
     *
     * @param patientId ID of the patient
     * @param timestamp Timestamp at which the data was generated
     * @param label     Label describing the type of data
     * @param data      The data of the patient
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        String message = String.format("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s", patientId, timestamp, label, data);
        // Broadcast the message to all connected clients
        System.out.println(message + ", server clients: " + server.getConnections().size());
        for (WebSocket conn : server.getConnections()) {
            conn.send(message);
        }
        System.out.println("outputted");
    }


    public void outputString(String data) {
        String message = String.format(data);
        // Broadcast the message to all connected clients
        System.out.print("output: " + message + ", server clients: " + server.getConnections().size());
        for (WebSocket conn : server.getConnections()) {
            conn.send(message);
        }
        System.out.println(", message send");
    }

    private static class SimpleWebSocketServer extends WebSocketServer {

        public SimpleWebSocketServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, org.java_websocket.handshake.ClientHandshake handshake) {
            System.out.println("New connection: " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            System.out.println("Closed connection: " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            // Not used in this context
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onStart() {
            System.out.println("Server started successfully");
        }
    }
}
