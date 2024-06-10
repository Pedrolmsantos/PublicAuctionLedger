package org.example.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.net.InetAddress;

public class Contact {
    private BigInteger nodeID;
    private InetAddress ipAddress;
    private int port;
    private BigInteger lastSeen;
    @JsonCreator
    public Contact(@JsonProperty("nodeID") BigInteger nodeID,@JsonProperty("ipAddress") InetAddress ipAddress,@JsonProperty("port") int port) {
        this.nodeID = nodeID;
        this.ipAddress = ipAddress;
        this.port = port;
        this.lastSeen = BigInteger.valueOf(System.currentTimeMillis());
    }
    public BigInteger getNodeID() {
        return nodeID;
    }


    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }
    public BigInteger getLastSeen() {
        return lastSeen;
    }

    public void updateLastSeen() {
        this.lastSeen = BigInteger.valueOf(System.currentTimeMillis());
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact contact = (Contact) o;
        return port == contact.port &&
                nodeID.equals(contact.nodeID) &&
                ipAddress.equals(contact.ipAddress);
    }

    @Override
    public int hashCode() {
        int result = nodeID.hashCode();
        result = 31 * result + ipAddress.hashCode();
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "nodeID=" + nodeID +
                ", ipAddress=" + ipAddress +
                ", port=" + port +
                '}';
    }
}
