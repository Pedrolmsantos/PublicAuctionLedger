
import org.example.crypto.CryptoUtils;
import org.example.network.Contact;
import org.example.network.Kademlia;
import org.example.network.Node;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KademliaTest {

    @Test
    void testMergeAndSortContacts() {
        BigInteger targetID = new BigInteger("5");
        Contact c1 = new Contact(new BigInteger("3"), InetAddress.getLoopbackAddress(), 3000);
        Contact c2 = new Contact(new BigInteger("7"), InetAddress.getLoopbackAddress(), 3001);
        Contact c3 = new Contact(new BigInteger("2"), InetAddress.getLoopbackAddress(), 3002);
        List<Contact> closestContacts = new ArrayList<>(Arrays.asList(c1, c2)); // Mutable copy
        List<Contact> newContacts = new ArrayList<>(Arrays.asList(c3));
       // Kademlia kademlia = new Kademlia(BigInteger.ZERO,c1); // Adjust if your constructor requires it
        //List<Contact> result = kademlia.mergeAndSortContacts(closestContacts, newContacts, targetID);
       // List<Contact> expected = Arrays.asList(c3, c1, c2);
       // assertEquals(expected, result);
    }
    @Test
    void testNodeStore(){

    }
    @Test
    void testNodeLeave() throws UnknownHostException {
        // Setup
        Node nodeA = new Node(new BigInteger("1"),InetAddress.getByName("127.0.0.1"),3000);
        Contact A = new Contact(new BigInteger(CryptoUtils.calculateNodeID("1").getBytes()),InetAddress.getLoopbackAddress(),6001);
        nodeA.bootstrap(A);
        // Assertions
       // assertFalse(nodeA.getProtocol().getRoutingTable().getBuckets().get(0).getContacts().contains(nodeB.getContact()));
        // ... Similar checks for other nodes/contacts
    }
}
