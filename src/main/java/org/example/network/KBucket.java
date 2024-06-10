package org.example.network;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KBucket {
    private int distancePrefix = 3;
    private final List<Contact> contacts;
    public KBucket(int distancePrefix) {
        this.distancePrefix = distancePrefix;
        this.contacts = new ArrayList<>();
    }
    public boolean hasSpace() {
        return contacts.size() < 3;
    }

    public void addContact(Contact contact, BigInteger yourNodeID) {
        BigInteger distance = yourNodeID.xor(contact.getNodeID());
        Boolean r = !distance.testBit(distancePrefix);
        //if(r){
        // Check if distance fits within the bucket's range using bitwise operations
       // if (distance.compareTo(BigInteger.TWO.pow(distancePrefix)) >= 0 &&
            //    distance.compareTo(BigInteger.TWO.pow(distancePrefix + 1)) < 0) {
            if (hasSpace()) {
                contacts.add(contact);
            } else {
                contacts.remove(0); // Remove the oldest (least recently used) contact
                contacts.add(contact); // Add the new contact
            }
        //}
    }
    public List<Contact> getContacts() {
        return contacts;
    }
    public Contact getRandomContact() {
        if (contacts.isEmpty()) {
            return null;  // Handle case where bucket is empty
        }
        Random random = new Random();
        int randomIndex = random.nextInt(contacts.size());
        return contacts.get(randomIndex);
    }

    public void removeContact(BigInteger nodeID) {
        contacts.remove(getContact(nodeID));
    }

    public int getDistancePrefix() {
        return distancePrefix;
    }

    public Contact getContact(BigInteger nodeID) {
        for (Contact contact : contacts) {
            if (contact.getNodeID().equals(nodeID)) {
                return contact;
            }
        }
        return null;
    }
}
