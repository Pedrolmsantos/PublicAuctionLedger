package org.example.auction;

import org.example.network.Node;

import java.math.BigInteger;

public class AuctionPublisher {
    private Node node;

    public AuctionPublisher(Node node) {
        this.node = node;
    }

    public void publishAuctionEvent(String topic, AuctionItem data) throws Exception {
        node.publish(topic, data);
    }
    public void publishAuctionEventBid(String topic, Bid data) throws Exception {
        node.publishbid(topic, data);
    }

    public void publishNewUserEvent(String name, BigInteger userid) throws Exception {
        node.publishUser("newUser",  userid);
    }
}
