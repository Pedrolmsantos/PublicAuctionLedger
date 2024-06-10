import org.example.crypto.CryptoUtils;
import org.example.network.Contact;
import org.example.network.Node;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Objects;

public class Node2 {
    public static void main(String[] args) {
        Node nodeB = new Node(new BigInteger(CryptoUtils.calculateNodeID("3").getBytes()), InetAddress.getLoopbackAddress(),6001);
        Contact A = new Contact(new BigInteger(CryptoUtils.calculateNodeID("1").getBytes()),InetAddress.getLoopbackAddress(),6000);
        nodeB.bootstrap(A);
        System.out.println("Nodeid:" + nodeB.getNodeID());
    }
}
