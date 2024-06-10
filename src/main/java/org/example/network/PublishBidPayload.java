package org.example.network;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.auction.Bid;

public class PublishBidPayload {
    public String topic;
    public Bid data;
    public PublishBidPayload(@JsonProperty("topic") String topic,@JsonProperty("data") Bid data) {
        this.topic = topic;
        this.data = data;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Bid getData() {
        return data;
    }

    public void setData(Bid data) {
        this.data = data;
    }
}
