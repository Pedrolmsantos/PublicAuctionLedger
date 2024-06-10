import org.example.crypto.CryptoUtils;
import org.example.network.Contact;
import org.example.network.Node;

import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class Node1 {

    public static void main(String[] args) {
        Node nodeA = new Node(new BigInteger(CryptoUtils.calculateNodeID("2").getBytes()), InetAddress.getLoopbackAddress(),6002);
        Contact A = new Contact(new BigInteger(CryptoUtils.calculateNodeID("1").getBytes()),InetAddress.getLoopbackAddress(),6000);
        nodeA.bootstrap(A);
        System.out.println("Nodeid:" + nodeA.getNodeID());
    }

}
