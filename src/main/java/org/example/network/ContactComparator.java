package org.example.network;

import java.math.BigInteger;
import java.util.Comparator;

public class ContactComparator implements Comparator<Contact> {
    private final BigInteger targetId;

    public ContactComparator(BigInteger targetId) {
        this.targetId = targetId;
    }

    @Override
    public int compare(Contact contact1, Contact contact2) {
        BigInteger distance1 = targetId.xor(contact1.getNodeID());
        BigInteger distance2 = targetId.xor(contact2.getNodeID());
        return distance1.compareTo(distance2);
    }
}

