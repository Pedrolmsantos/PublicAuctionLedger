package org.example.network;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoutingTable {
    private static final long MAX_STALE_TIME = 3600000;
    private List<KBucket> buckets;
    private final int k;


    public RoutingTable(int k) {
        this.k = k;
        this.buckets = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            buckets.add(new KBucket(k));
        }
    }

    public void update(Contact contact, BigInteger yourNodeID) {
        BigInteger distance = yourNodeID.xor(contact.getNodeID());
        int bucketIndex = getBucketIndex(distance);
        if (bucketIndex == -1) {
            int newBucketIndex = buckets.size();
            KBucket newBucket = new KBucket(newBucketIndex);
            buckets.add(newBucket);
            bucketIndex = newBucketIndex;
        }
        KBucket bucket = buckets.get(bucketIndex);
        for(Contact con : bucket.getContacts())
            if(con.getNodeID().equals(contact.getNodeID())){
                con.updateLastSeen();
                return;
            }
        System.out.println("Added " + contact.getNodeID().toString() + "to the routing table");
        bucket.addContact(contact, yourNodeID);
    }

    public void remove(BigInteger nodeID, BigInteger yourNodeID) {
        BigInteger distance = yourNodeID.xor(nodeID);
        int bucketIndex = getBucketIndex(distance);
        KBucket bucket = buckets.get(bucketIndex);
        bucket.removeContact(nodeID);
    }
    public void printallcontacts(){
        System.out.println("Printing all contacts");
        for(KBucket bkt : buckets){
            for(Contact cnt : bkt.getContacts()){
                System.out.println(cnt.toString());
            }
        }
    }
    public List<Contact> getallcontacts(){
        List<Contact> allcontacts = new ArrayList<>();
        for(KBucket bkt : buckets){
            allcontacts.addAll(bkt.getContacts());
        }
        return allcontacts;
    }
    public List<KBucket> getBuckets() {
        return buckets;
    }
    private int getBucketIndex(BigInteger xor) {
        int prefixLength = xor.bitLength() - 1;
        int bucketIndex = Math.max(0, prefixLength) % getBuckets().size();
        return bucketIndex;
    }
    public void checkAndEvictStaleNodes() {
        long currentTime = System.currentTimeMillis();
        long threshold = currentTime - MAX_STALE_TIME;
        for (KBucket bucket : buckets) {
            List<Contact> contacts = bucket.getContacts();
            contacts.removeIf(contact -> contact.getLastSeen().longValue() < threshold);
        }
    }
}
