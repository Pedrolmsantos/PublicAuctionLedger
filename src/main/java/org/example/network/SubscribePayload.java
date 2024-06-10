package org.example.network;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;

public class SubscribePayload {
    private String topic;
    private BigInteger nodeID;

    public SubscribePayload(@JsonProperty("topic") String topic,@JsonProperty("nodeID") BigInteger nodeID) {
        this.topic = topic;
        this.nodeID = nodeID;
    }

    public String getTopic() {
        return topic;
    }

    public BigInteger getNodeID() {
        return nodeID;
    }
}
