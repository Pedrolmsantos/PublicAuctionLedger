package org.example.auction;

import org.example.network.Node;

public class AuctionSubscriber {
    private Node node;

    public AuctionSubscriber(Node node) {
        this.node = node;
    }

    public void subscribeToAuction(String topic) {
        node.subscribe(topic, node.getNodeID());
    }

    public void handleAuctionEvent(String event) {
        // Handle the event (e.g., update UI, log event)
        System.out.println("Handling auction event: " + event);
    }
}
