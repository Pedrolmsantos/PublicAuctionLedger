package org.example.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.example.auction.Auction;
import org.example.auction.AuctionItem;
import org.example.auction.Bid;
import org.example.auction.User;
import org.example.blockchain.Block;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class IncommingPacketHandler extends  SimpleChannelInboundHandler<DatagramPacket> {
    Node node;
    IncommingPacketHandler(String parserServer, Node node){
        this.node = node;
    }
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, DatagramPacket packet) throws Exception {
        node.getRpc().didReceiveResponse();
        final InetAddress srcAddr = packet.sender().getAddress();
        final ByteBuf buf = packet.content();
        final int rcvPktLength = buf.readableBytes();
        final byte[] rcvPktBuf = new byte[rcvPktLength];
        buf.readBytes(rcvPktBuf);
        //System.out.println("Inside incomming packet handler");
        //System.out.println("Received packet from " + srcAddr + " with length " + rcvPktLength);
        //System.out.println("Received packet: " + new String(rcvPktBuf));
        Gson gson = new Gson();
        byte[] buf1 = new byte[256];
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        RPC.RPCMessage receivedMessage = null;
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(RPC.RPCMessage.class, new KademliaMessageDeserializer());

        mapper.registerModule(module);

        try {
            RPC.RPCMessage message = mapper.readValue(new String(rcvPktBuf), RPC.RPCMessage.class);
            receivedMessage = message;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Received message: " + new String(rcvPktBuf) + " (Sender- " + srcAddr.toString() +":" + packet.sender().getPort());
        //System.out.println("Serialize:" + receivedMessage.toString());
        switch (receivedMessage.getMessageType()) {
            case RPC.PUBLISH_REQUEST -> handlePublishRequest(receivedMessage.getPayload(), receivedMessage.getOrigin());
            case RPC.PUBLISH_BID_REQUEST -> handleBid(receivedMessage.getPayload(), receivedMessage.getOrigin());
            case RPC.SUBSCRIBE_REQUEST -> handleSubscribeRequest(receivedMessage.getPayload(), receivedMessage.getOrigin());
            case RPC.FIND_NODE_REQUEST -> handleFindNodeRequest(receivedMessage.getPayload(), receivedMessage.getOrigin());
            case RPC.STORE_REQUEST -> handleStoreRequest(receivedMessage.getPayload(), receivedMessage.getOrigin());
            case RPC.FIND_VALUE_REQUEST -> handleFindValueRequest(receivedMessage.getPayload(), receivedMessage.getOrigin());
            case RPC.PING_REQUEST -> handlePingRequest(receivedMessage.getPayload(), receivedMessage.getOrigin());
            case RPC.FIND_VALUE_RESPONSE -> handleFindValueResponse(receivedMessage.getPayload(), receivedMessage.getOrigin());
            case RPC.FIND_NODE_RESPONSE -> handleFindNodeResponse(receivedMessage.getPayload());
            case RPC.PING_RESPONSE -> handlePingResponse(receivedMessage.getOrigin());
            case RPC.FIND_VALUE_RESPONSE_NF -> handleFindValueResponseNF(receivedMessage.getPayload(), receivedMessage.getOrigin());
            case RPC.NODE_LEAVE_REQUEST -> handleNodeLeaveRequest(receivedMessage.getPayload(), receivedMessage.getOrigin());
            case RPC.SEND_DEBIT_REQUEST -> handleDebitRequest(receivedMessage.getPayload(), receivedMessage.getOrigin());
            case RPC.SEND_NEW_USER_REQUEST -> handleNewUser(receivedMessage.getPayload(), receivedMessage.getOrigin());
            default -> System.out.println("Unknown message type: " + receivedMessage.getMessageType());
        }
    }

    private void handleNewUser(Object payload, Contact origin) throws Exception {
        BigInteger newUser = (BigInteger) payload;
        String UserData = String.format("User-%s", newUser);
        Block newBlock = new Block(node.getBlockchain().chain.size(), UserData, node.getBlockchain().getLatestBlock().hash);
        newBlock.addTransaction(UserData);
        node.getBlockchain().addBlock(newBlock);
        //node.getUsers().put(newUser, new User(newUser.toString(), newUser));
    }

    private void handleDebitRequest(Object payload, Contact origin) {
        PublishBidPayload publishPayload = (PublishBidPayload) payload;
        String transactionData = publishPayload.getData().getUserid() + "-DEBIT-" + publishPayload.getData().getAmount();
        Block newBlock = new Block(node.getBlockchain().chain.size(), transactionData, node.getBlockchain().getLatestBlock().hash);
        newBlock.addTransaction(transactionData);
        node.getBlockchain().addBlock(newBlock);
    }

    private void handleBid(Object payload, Contact origin) {
        PublishBidPayload publishPayload = (PublishBidPayload) payload;
        AuctionItem a = node.getActiveAuctions().get(publishPayload.getTopic());
        String bidData = String.format("Bid-%s-%s-%.2f", publishPayload.getTopic(), publishPayload.getData().getUserid(), publishPayload.getData().getAmount());
        Block newBlock = new Block(node.getBlockchain().chain.size(), bidData, node.getBlockchain().getLatestBlock().hash);
        newBlock.addTransaction(bidData);
        node.getBlockchain().addBlock(newBlock);
        a.setHighestBid(publishPayload.getData().getAmount());
        a.setHighestBidder(publishPayload.getData().getUserid());
    }

    private void handlePublishRequest(Object payload, Contact origin) {
        PublishPayload publishPayload = (PublishPayload) payload;
        if(publishPayload.getEvent() instanceof Bid){
            handleBid(publishPayload, origin);
            return;
        }
        node.handlePublishEvent(publishPayload);
        node.getRoutingTable(publishPayload.getTopic()).update(new Contact(origin.getNodeID(), origin.getIpAddress(), origin.getPort()), node.getNodeID());
    }
    private void handleSubscribeRequest(Object payload, Contact origin) {
        SubscribePayload subscribePayload = (SubscribePayload) payload;
        Contact newSubscriber = new Contact(subscribePayload.getNodeID(), origin.getIpAddress(), origin.getPort());
        node.getRoutingTable(subscribePayload.getTopic()).update(newSubscriber, subscribePayload.getNodeID());
        String currentValue = node.getLocalValue(subscribePayload.getTopic(), subscribePayload.getNodeID());

        if (currentValue != null) {
            //node.getRpc().send(newSubscriber, new RPC.RPCMessage(RPC.PUBLISH_REQUEST,node.getProtocol().selfcontact, new PublishPayload(subscribePayload.getTopic(),(Auction) currentValue)));
        }
    }


    private void handleFindValueResponse(Object payload, Contact origin) {
        StorePayload value = (StorePayload) payload;
        node.storeValue(value.getKey(), value.getValue());
        System.out.println("Value found: " + value.toString());
    }

    private void handleFindValueResponseNF(Object payload, Contact origin) {
        List<Contact> closerContacts = (List<Contact>) payload;
        for (Contact contact : closerContacts) {
            node.getProtocol().getRoutingTable().update(contact, node.getNodeID());
        }
        Set<Contact> queriedContacts = node.getProtocol().getQueriedContacts();
        List<Contact> selectedContacts = new ArrayList<>();
        for (Contact contact : closerContacts) {
            if (!queriedContacts.contains(contact) && selectedContacts.size() < node.getProtocol().getK()) {
                selectedContacts.add(contact);
                node.getProtocol().getQueriedContacts().add(contact);
            }
        }
        for (Contact nextContact : selectedContacts) {
            node.getProtocol().sendFindValueRequest(nextContact, (BigInteger) payload);
        }
    }

    private void handlePingRequest(Object payload, Contact origin) {
        System.out.println("Got Ping from ID: " + origin.toString());
        BigInteger senderID = (BigInteger) payload;
        node.getProtocol().getRoutingTable().update(new Contact(senderID, origin.getIpAddress(), origin.getPort()), node.getNodeID());
        RPC.RPCMessage response = new RPC.RPCMessage(RPC.PING_RESPONSE,node.getContact(), true);
        node.getRpc().send(new Contact(senderID, origin.getIpAddress(), origin.getPort()), response);
        node.getProtocol().getRoutingTable().printallcontacts();
    }
    private void handleFindValueRequest(Object payload, Contact origin) {
        BigInteger key = (BigInteger) payload;
        String value = node.getLocalValue(key);
        StorePayload foundValue = new StorePayload(key, value);
        if (value != null) {
            sendFindValueResponse(foundValue,origin);
            return;
        }
        List<Contact> closestContacts = node.getProtocol().findClosestContacts(key);
        for (Contact contact : closestContacts) {
            node.getProtocol().getRoutingTable().update(contact, node.getNodeID());
        }
        RPC.RPCMessage response = new RPC.RPCMessage(RPC.FIND_VALUE_RESPONSE_NF,node.getContact(), closestContacts);
        node.getRpc().send(new Contact(origin.getNodeID(), origin.getIpAddress(), origin.getPort()), response);
    }

    private void sendFindValueResponse(Object value, Contact origin) {
        StorePayload v = (StorePayload) value;
        RPC.RPCMessage response = new RPC.RPCMessage(RPC.FIND_VALUE_RESPONSE,node.getContact(), v);
        node.getRpc().send(new Contact(origin.getNodeID(), origin.getIpAddress(), origin.getPort()), response);
    }
    private void handleFindNodeRequest(Object payload, Contact origin) {
        System.out.println("Received Find Node Request for node ID: " + payload.toString());
        BigInteger nodeID = (BigInteger) payload;
        List<Contact> closestContacts = node.getProtocol().findClosestContacts(nodeID);
        for (AuctionItem auctionItem : node.getActiveAuctions().values()) {
            RPC.RPCMessage response = new RPC.RPCMessage(RPC.PUBLISH_REQUEST,node.getContact(), new PublishPayload("auction:" + auctionItem.getItem(), auctionItem));
            node.getRpc().send(new Contact(nodeID, origin.getIpAddress(), origin.getPort()), response);
        }
        RPC.RPCMessage response = new RPC.RPCMessage(RPC.FIND_NODE_RESPONSE,node.getContact(), closestContacts);
        node.getRpc().send(new Contact(nodeID, origin.getIpAddress(), origin.getPort()), response);
    }

    private void handleStoreRequest(Object payload, Contact origin) {
        System.out.println("Received Store Request and saved: "+ payload.toString());
        StorePayload kv = (StorePayload) payload;
        node.storeValue(kv.getKey(), kv.getValue());
        RPC.RPCMessage response = new RPC.RPCMessage(RPC.STORE_RESPONSE,node.getContact(), true); // Assuming a boolean success payload
        node.getRpc().send(new Contact(new BigInteger("1"), origin.getIpAddress(), origin.getPort()), response);
    }
    private void handleFindNodeResponse(Object payload) {
        List<Contact> contacts = (List<Contact>) payload;
        for (Contact contact : contacts) {
            node.getRpc().send(contact,new RPC.RPCMessage(RPC.PING_REQUEST,node.getContact(), node.getNodeID()));
        }
    }
    private void handlePingResponse(Contact origin) {
        System.out.println("Got Pong from ID: " + origin.getNodeID());
        node.getProtocol().getRoutingTable().update(origin, node.getNodeID());
        node.getProtocol().getRoutingTable().printallcontacts();
    }

    private void handleNodeLeaveRequest(Object payload, Contact origin) {
        BigInteger nodeID = (BigInteger) payload;
        node.getProtocol().getRoutingTable().remove(nodeID, node.getNodeID());
        RPC.RPCMessage response = new RPC.RPCMessage(RPC.NODE_LEAVE_RESPONSE,node.getContact(), true);
        node.getRpc().send(new Contact(nodeID, origin.getIpAddress(), origin.getPort()), response);
    }
}