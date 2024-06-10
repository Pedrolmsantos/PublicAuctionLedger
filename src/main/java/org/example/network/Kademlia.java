package  org.example.network;

import java.math.BigInteger;
import java.util.*;

public class Kademlia {
    private RoutingTable routingTable;
    private BigInteger yourNodeID;
    private Set<Contact> queriedContacts = new HashSet<>();
    public boolean valueFound;
    private static final int k = 3;
    Contact selfcontact;
    Node node;
    RPC rpc;
    public Kademlia(BigInteger yourNodeID, Contact selfcontact, RPC rpc,Node node) {
        this.yourNodeID = yourNodeID;
        this.routingTable = new RoutingTable(k);
        this.node = node;
        this.selfcontact = selfcontact;
        this.rpc = rpc;
        valueFound = false;
    }
    public void sendFindNodeRequest(Contact destination, BigInteger targetID) {
        RPC.RPCMessage message = new RPC.RPCMessage(RPC.FIND_NODE_REQUEST,selfcontact, targetID);
        rpc.send(destination, message);
        System.out.println("Sending find node request to " + destination.getIpAddress() + ":" + destination.getPort());
    }
    public void sendStoreRequest(Contact destination, StorePayload payload) {
        RPC.RPCMessage message = new RPC.RPCMessage(RPC.STORE_REQUEST,selfcontact, payload);
        rpc.send(destination, message);
        System.out.println("Sending store request to " + destination.getIpAddress() + ":" + destination.getPort());
    }

    public static int getK() {
        return k;
    }
    public RoutingTable getRoutingTable() {
        return routingTable;
    }
    public List<Contact> findClosestContacts(BigInteger targetID) {
        List<Contact> closestContacts = new ArrayList<>();
        int bucketIndex = getBucketIndex(targetID.xor(yourNodeID));
        for (int i = bucketIndex - 1, j = bucketIndex + 1; i >= 0 || j < routingTable.getBuckets().size(); i--, j++) {
            if (i >= 0) {
                closestContacts.addAll(routingTable.getBuckets().get(i).getContacts());
            }
            if (j < routingTable.getBuckets().size()) {
                closestContacts.addAll(routingTable.getBuckets().get(j).getContacts());
            }
            Collections.sort(closestContacts, new ContactComparator(targetID));
            if (closestContacts.size() >= k) {
                return closestContacts.subList(0, k);
            }
        }
        return closestContacts;
    }

    public int getBucketIndex(BigInteger xor){
        int prefixLength = xor.bitLength() - 1;
        int bucketIndex = Math.max(0, prefixLength) % routingTable.getBuckets().size();  // Normalize using modulo
        return bucketIndex;
    }

    public void sendFindValueRequest(Contact contact, BigInteger key) {
        RPC.RPCMessage message = new RPC.RPCMessage(RPC.FIND_VALUE_REQUEST,selfcontact, key);
        node.getRpc().send(new Contact(yourNodeID, node.getIpAddress(), node.getPort()), message);
        System.out.println("Sending find value request to " + contact.getIpAddress() + ":" + contact.getPort());
    }

    public Set<Contact> getQueriedContacts() {
        return queriedContacts;
    }

    public void sendNodeLeaveRequest(Contact contact, BigInteger nodeDest){
        RPC.RPCMessage message = new RPC.RPCMessage(RPC.NODE_LEAVE_REQUEST,selfcontact, nodeDest);
        RPC.RPCMessage response = rpc.send(contact, message);
           System.out.println("Node leave request sent to " + contact.getIpAddress() + ":" + contact.getPort());
    }
}