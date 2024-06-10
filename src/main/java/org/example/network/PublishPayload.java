package org.example.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.auction.AuctionItem;

public class PublishPayload {
    public String topic;
    public AuctionItem event;

    public PublishPayload(@JsonProperty("topic") String topic,@JsonProperty("event") AuctionItem event) {
        this.topic = topic;
        this.event = event;
    }

    public String getTopic() {
        return topic;
    }

    public Object getEvent() {
        return event;
    }

}
