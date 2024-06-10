package org.example.network;
import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.*;

import io.netty.bootstrap.Bootstrap;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.ChannelPipeline;
import org.example.auction.Auction;
import org.example.auction.AuctionItem;
import org.example.auction.Bid;
import org.example.auction.User;
import org.example.blockchain.Block;
import org.example.blockchain.Blockchain;

import java.net.InetAddress;

public class Node {
    private final BigInteger nodeID;
    private final InetAddress ipAddress;
    private final int port;
    private ServerSocket serverSocket;
    private boolean shouldListen;
    private Kademlia protocol;

    private Blockchain blockchain;

    private HashMap<BigInteger, String> dataStore;

    private final Map<String, RoutingTable> topicRoutingTables;

    private final Map<String, Map<BigInteger, String>> topicStorage;

    private Map<String, AuctionItem> activeAuctions; // Track active auctions
    private Map<BigInteger, User> users; // Track users
    private Contact selfContact;
    private RPC rpc;

    public Node(BigInteger nodeID, InetAddress ipAddress, int port) {
        this.nodeID = nodeID;
        this.ipAddress = ipAddress;
        this.port = port;
        this.activeAuctions = new HashMap<>();
        this.users = new HashMap<>();
        selfContact = new Contact(nodeID, ipAddress, port);
        this.dataStore = new HashMap<>();
        this.topicRoutingTables = new HashMap<>();
        this.topicStorage = new HashMap<>();
        rpc = new RPC(selfContact, this);
        this.blockchain = new Blockchain(0); // difficulty level 4
        protocol = new Kademlia(nodeID, selfContact, rpc, this);
        //routingTable = new RoutingTable(8);
        protocol.getRoutingTable().update(selfContact, nodeID);
    }

    public RoutingTable getRoutingTable(String topic) {
        return topicRoutingTables.computeIfAbsent(topic, k -> new RoutingTable( 8)); // 8 is the bucket capacity
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public void storeValue(String topic, BigInteger key, String value) {
        topicStorage.computeIfAbsent(topic, k -> new HashMap<>()).put(key, value);
    }

    public String getLocalValue(String topic, BigInteger key) {
        return topicStorage.getOrDefault(topic, new HashMap<>()).get(key);
    }
    public BigInteger getNodeID() {
        return nodeID;
    }


    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public Kademlia getProtocol() {
        return protocol;
    }

    public Node getNodeInst() {
        return this;
    }

    public int getPort() {
        return port;
    }

    public RPC getRpc() {
        return rpc;
    }



    public BigInteger calculateDistance(Node otherNode) {
        return this.nodeID.xor(otherNode.nodeID);
    }

    public void publish(String topic, AuctionItem event) throws Exception {
        List<Contact> contacts = protocol.getRoutingTable().getallcontacts();
        for (Contact contact : contacts) {
            RPC.RPCMessage message = rpc.send(contact, new RPC.RPCMessage(RPC.PUBLISH_REQUEST,selfContact,new PublishPayload(topic, event)));
            rpc.send(contact, message);
        }
    }

    public Map<String, AuctionItem> getActiveAuctions() {
        return activeAuctions;
    }

    public void setActiveAuctions(Map<String, AuctionItem> activeAuctions) {
        this.activeAuctions = activeAuctions;
    }

    public Map<BigInteger, User> getUsers() {
        return users;
    }

    public void setUsers(Map<BigInteger, User> users) {
        this.users = users;
    }

    public void subscribe(String topic, BigInteger nodeID) {
        // Logic to subscribe to a topic
        // Sending a subscribe message to all nodes in the DHT
        List<Contact> contacts = getRoutingTable(topic).getallcontacts();
        for (Contact contact : contacts) {
            rpc.send(contact, new RPC.RPCMessage(RPC.SUBSCRIBE_REQUEST,selfContact, new SubscribePayload(topic, nodeID)));
        }
    }

    public void handlePublishEvent(PublishPayload payload) {
        // Handle the publish event, e.g., store the event or forward it to subscribers
        AuctionItem a = (AuctionItem) payload.getEvent();
        System.out.println("active auctions: " + activeAuctions.toString());
        if(activeAuctions.containsKey(payload.getTopic())){
            //System.out.println("Auction already exists");
            return;
        }
        String auctionData = String.format("Auction-%s-auction:%s-%.2f-%s", getNodeID(), a.getItem(), a.getStartingPrice(), new Date(a.endTime));
        Block newBlock = new Block(getBlockchain().chain.size(), auctionData, getBlockchain().getLatestBlock().hash);
        newBlock.addTransaction(auctionData);
        getBlockchain().addBlock(newBlock);
        activeAuctions.put(payload.getTopic(),a);
        System.out.println("Received event$: " + payload.getTopic() + payload.getEvent().toString());
        storeValue(payload.getTopic(), nodeID, payload.getEvent().toString());
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return nodeID.equals(node.nodeID);
    }

    @Override
    public int hashCode() {
        return nodeID.hashCode();
    }

    @Override
    public String toString() {
        return "Node{" +
                "nodeID=" + nodeID +
                ", ipAddress=" + ipAddress +
                ", port=" + port +
                '}';
    }

    public void startListening() {
        this.shouldListen = true;
        new ListenerThread(rpc).start();  // Start the listener thread
    }

    public void stopListening() {
        try {
            this.shouldListen = false;
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void bootstrap(Contact knownNode) {
        new EvicStaleNodesTh().start();
        startListening();
        protocol.getRoutingTable().update(knownNode, nodeID);
        protocol.sendFindNodeRequest(knownNode, nodeID);
    }

    public void storeValue(BigInteger key, String value) {
        dataStore.put(key, value);
    }

    public String getLocalValue(BigInteger key) {
        return dataStore.get(key);
    }

    public void leaveNetwork() {
        for (KBucket bucket : protocol.getRoutingTable().getBuckets()) {
            for (Contact contact : bucket.getContacts()) {
                protocol.sendNodeLeaveRequest(contact, this.nodeID);
            }
        }
        stopListening();
    }

    public Contact getContact() {
        return selfContact;
    }

    public void bootstrapRootNode() {
        new EvicStaleNodesTh().start();
        startListening();
    }

    public void publishbid(String topic, Bid data) {
        // Get all contacts from the topic routing table
        String aux = "auction:" + topic;
        if(topicRoutingTables.get("auction:" + topic) != null) {
            for(Contact contact : topicRoutingTables.get(aux).getallcontacts()) {
                RPC.RPCMessage message = rpc.send(contact, new RPC.RPCMessage(RPC.PUBLISH_BID_REQUEST,selfContact,new PublishBidPayload(aux, data)));
                rpc.send(contact, message);
            }
        }
        activeAuctions.get(aux).setHighestBid(data.getAmount());
        activeAuctions.get(aux).setHighestBidder(data.getUserid());
    }

    public void publishUser(String newUser, BigInteger userid) {
        List<Contact> contacts = protocol.getRoutingTable().getallcontacts();
        contacts.remove(selfContact);
        for (Contact contact : contacts) {
            RPC.RPCMessage message = rpc.send(contact, new RPC.RPCMessage(RPC.SEND_NEW_USER_REQUEST,selfContact,userid));
            rpc.send(contact, message);
        }
    }

    private class ListenerThread extends Thread {
        RPC rpc;
        public ListenerThread(RPC rpc) {
            this.rpc = rpc;
        }
        @Override
        public void run() {
            final NioEventLoopGroup group = new NioEventLoopGroup();
            try {
                final Bootstrap b = new Bootstrap();
                b.group(group).channel(NioDatagramChannel.class)
                        .option(ChannelOption.SO_BROADCAST, true)
                        .handler(new ChannelInitializer<NioDatagramChannel>() {
                            @Override
                            public void initChannel(final NioDatagramChannel ch) throws Exception {
                                rpc.didReceiveResponse();
                                ChannelPipeline p = ch.pipeline();
                                p.addLast(new IncommingPacketHandler("Actor", getNodeInst()));
                            }
                        });
                System.out.printf("waiting for message %d %s", getPort(), String.format(getIpAddress().toString()));
                b.bind(getIpAddress(), getPort()).sync().channel().closeFuture().await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.print("In Server Finally");
            }
        }
    }

    private class EvicStaleNodesTh extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                protocol.getRoutingTable().checkAndEvictStaleNodes();
            }
        }
    }
}
