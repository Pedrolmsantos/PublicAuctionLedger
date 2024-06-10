package org.example.network;

import java.net.*;
import java.io.IOException;

import com.google.gson.Gson;

public class RPC {
    public static final int FIND_NODE_REQUEST = 1;
    public static final int FIND_NODE_RESPONSE = 2;
    public static final int FIND_VALUE_REQUEST = 3;
    public static final int FIND_VALUE_RESPONSE = 4;
    public static final int PING_REQUEST = 5;
    public static final int PING_RESPONSE = 6;
    public static final int STORE_REQUEST = 7;
    public static final int STORE_RESPONSE = 8;
    public static final int NODE_LEAVE_REQUEST = 9;
    public static final int NODE_LEAVE_RESPONSE = 10;
    public static final int FIND_VALUE_RESPONSE_NF = 11;
    public static final int PUBLISH_REQUEST = 12;
    public static final int SUBSCRIBE_REQUEST = 13;
    public static final int PUBLISH_BID_REQUEST = 14;
    public static final int SEND_DEBIT_REQUEST = 15;
    public static final int SEND_NEW_USER_REQUEST = 16;

    private static final int TIMEOUT_MS = 2000;
    private static final int MAX_RETRIES = 3;

    private volatile boolean receivedResponse = false;
    private final Object responseLock = new Object();
    private final Contact selfcontact;
    public static class RPCMessage {
        private int messageType;
        private Contact origin;
        private Object payload;

        public RPCMessage(int messageType,Contact origin, Object payload) {
            this.messageType = messageType;
            this.origin = origin;
            this.payload = payload;
        }

        public int getMessageType() {
            return messageType;
        }

        public Object getPayload() {
            return payload;
        }
        public Contact getOrigin() {
            return origin;
        }
        public void setMessageType(int messageType) {
            this.messageType = messageType;
        }

        public void setPayload(Object payload) {
            this.payload = payload;
        }

        @Override
        public String toString() {
            return "RPCMessage{" +
                    "messageType=" + messageType +
                    ", payload=" + payload +
                    '}';
        }
    }
    // Methods
    private Node node;

    public RPC(Contact selfcontact,Node node) {
        this.selfcontact = selfcontact;
        this.node = node;
    }
    public void didReceiveResponse() {
        synchronized (responseLock) {
            receivedResponse = true;
            responseLock.notifyAll();
        }
    }

    public boolean getReceivedResponse() {
        synchronized (responseLock) {
            return receivedResponse;
        }
    }
    public RPCMessage send(Contact destination, RPCMessage message) {
        Gson gson = new Gson();
        byte[] buf = new byte[256];
        DatagramSocket socket = null;
        try {
            socket =  new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        String jsonMessage = gson.toJson(message);
        byte[] messageBytes = jsonMessage.getBytes();

        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, destination.getIpAddress(), destination.getPort());
        try {
            int retries = 0;
            while (retries < 1) {
                try { Thread.sleep(500); } catch (InterruptedException e) {}
                socket.send(packet);
                return message;

                //synchronized (responseLock) {
               //     receivedResponse = false;
             //   }
                //synchronized (responseLock) {
               //     responseLock.wait(TIMEOUT_MS);
               // }
                //if (getReceivedResponse()) {
             //       System.out.println("Received response");
              //      return message;
             //   }
               // System.out.println(packet + " timed out, retrying...");
              //  retries++;
            }
            return message;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

