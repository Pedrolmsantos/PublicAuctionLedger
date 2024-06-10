package org.example;

import org.example.auction.AuctionSystem;
import org.example.crypto.CryptoUtils;
import org.example.network.Contact;
import org.example.network.Node;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Random;

public class AuctionProjectApplication {
    public static void main(String[] args) {
        Node node = new Node(new BigInteger(CryptoUtils.generateNodeID().getBytes()), InetAddress.getLoopbackAddress(),new Random().nextInt(7000 - 3000) + 3000);
        node.bootstrap(new Contact(new BigInteger(CryptoUtils.calculateNodeID("1").getBytes()),InetAddress.getLoopbackAddress(),6000));
        System.out.flush();
        AuctionSystem auctionSystem = new AuctionSystem(node);
        auctionSystem.start();
    }
}