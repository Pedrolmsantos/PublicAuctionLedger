import org.example.crypto.CryptoUtils;
import org.example.network.Contact;
import org.example.network.Node;

import java.math.BigInteger;
import java.net.InetAddress;

public class RootNode {
    public static void main(String[] args) {
        Node nodeA = new Node(new BigInteger(CryptoUtils.calculateNodeID("1").getBytes()), InetAddress.getLoopbackAddress(),6000);
        nodeA.bootstrapRootNode();
        System.out.println("Nodeid:" + nodeA.getNodeID());
    }
}
