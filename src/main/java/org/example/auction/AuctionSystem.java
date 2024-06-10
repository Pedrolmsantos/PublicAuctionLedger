package org.example.auction;

import org.example.blockchain.Block;
import org.example.blockchain.Blockchain;
import org.example.crypto.CryptoUtils;
import org.example.network.Contact;
import org.example.network.Node;

import java.math.BigInteger;
import java.net.InetAddress;
import java.security.*;
import java.util.*;

public class AuctionSystem {

    private AuctionPublisher auctionPublisher;
    private AuctionSubscriber auctionSubscriber;
    private User currentUser; // Track the current user
    private Node node;

    public AuctionSystem(Node node) {
        this.auctionPublisher = new AuctionPublisher(node);
        this.auctionSubscriber = new AuctionSubscriber(node);
        this.node = node;
        createUser();
    }

    private void createUser() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your name:");
        String userName = scanner.nextLine();

        try {
            User user = new User(userName,node.getNodeID());
            node.getUsers().put(user.getUserid(), user);
            currentUser = user;
            auctionPublisher.publishNewUserEvent(user.getName(), user.getUserid());
            String UserData = String.format("User-%s", user.getUserid());
            Block newBlock = new Block(node.getBlockchain().chain.size(), UserData, node.getBlockchain().getLatestBlock().hash);
            newBlock.addTransaction(UserData);
            node.getBlockchain().addBlock(newBlock);
            System.out.println("User created: " + userName + " with initial balance " + node.getBlockchain().INITIAL_BALANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        //node.getRpc().send(node.)
        System.out.flush();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1. Create Auction");
            System.out.println("2. Place Bid");
            System.out.println("3. Subscribe to Auction");
            System.out.println("4. View Blockchain");
            System.out.println("5. Exit");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    createAuction(scanner);
                    break;
                case 2:
                    placeBid(scanner);
                    break;
                case 3:
                    subscribeToAuction(scanner);
                    break;
                case 4:
                    viewBlockchain();
                    break;
                case 5:
                    node.leaveNetwork();
                    return;
                default:
                    System.out.println("Invalid choice");
            }
        }
    }

    private void createAuction(Scanner scanner) {
        try {
            System.out.flush();
            System.out.println("Enter item name:");
            String item = scanner.nextLine();
            System.out.println("Enter starting price:");
            double startingPrice = scanner.nextDouble();

            String seller = currentUser.getName();
            PrivateKey privateKey = currentUser.getPrivateKey();
            PublicKey publicKey = currentUser.publicKey;
            long endTime = System.currentTimeMillis() + 2 * 60 * 1000; // 10 minutes from now
            AuctionItem auctionItem = new AuctionItem(node.getNodeID(), item, startingPrice, endTime);
            Auction auction = new Auction(auctionItem, privateKey, publicKey, node);
            if (!auction.verifyAuctionData()) {
                throw new Exception("Auction data verification failed.");
            }

            node.getActiveAuctions().put("auction:"+ item, auction.getItem());
            String auctionData = String.format("Auction-%s-auction:%s-%.2f-%s", node.getNodeID(), item, startingPrice, new Date(endTime));
            Block newBlock = new Block(node.getBlockchain().chain.size(), auctionData, node.getBlockchain().getLatestBlock().hash);
            newBlock.addTransaction(auctionData);
            node.getBlockchain().addBlock(newBlock);
            // Publish auction event
            //auctionPublisher.publishAuctionEvent(auction.auctionDataSignature,auctionData,currentUser.getUserid());
            auctionPublisher.publishAuctionEvent("auction:" + item, auction.getItem());
            System.out.println("Auction created and published!");
            new EndAuction(auction,item).start();
            // Schedule auction end

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void placeBid(Scanner scanner) {
        System.out.flush();
        try {
            System.out.println("Active Auction List: ");
            for(AuctionItem auction : node.getActiveAuctions().values()){
                System.out.println("- " + auction.item);
            }
            System.out.println("Enter item name:");
            String item = scanner.nextLine();
            System.out.println("Enter bid amount:");
            double bidAmount = scanner.nextDouble();
            AuctionItem auction = node.getActiveAuctions().get("auction:" + item.trim());
            if (auction == null) {
                System.out.println("Auction not found.");
                return;
            }

            if (System.currentTimeMillis() > auction.getEndTime()) {
                System.out.println("Auction has ended.");
                return;
            }

            if (bidAmount <= auction.getHighestBid()) {
                System.out.println("Bid amount must be higher than the current highest bid.");
                return;
            }

            BigInteger bidder = currentUser.getUserid();
            User bidderUser = node.getUsers().get(bidder);
            if (bidderUser == null) {
                System.out.println("Bidder not found.");
                return;
            }

            double currentBalance = node.getBlockchain().getUserBalance(bidder);
            System.out.println("Current balance for : " + bidder + " is " + currentBalance);
            if (bidAmount > currentBalance) {
                System.out.println("Insufficient balance.");
                return;
            }

            auction.setHighestBid(bidAmount);
            auction.setHighestBidder(bidder);

            String bidData = String.format("Bid-%s-%s-%.2f", "bid:" + item.trim(), bidder, bidAmount);
            Block newBlock = new Block(node.getBlockchain().chain.size(), bidData, node.getBlockchain().getLatestBlock().hash);
            newBlock.addTransaction(bidData);
            node.getBlockchain().addBlock(newBlock);

            // Publish bid event
            auctionPublisher.publishAuctionEventBid(item, new Bid(bidder,bidAmount));
            System.out.println("Bid placed and published!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void subscribeToAuction(Scanner scanner) {
        for(AuctionItem auction : node.getActiveAuctions().values()){
            System.out.println("Active Auction List: ");
            System.out.println("- " + auction.item);
        }
        System.out.println("Enter auction item name to subscribe:");
        String item = scanner.nextLine();

        auctionSubscriber.subscribeToAuction("auction:" + item);
        System.out.println("Subscribed to auction and bid events for " + item);
    }

    private void viewBlockchain() {
        for (Block block : node.getBlockchain().chain) {
            System.out.println("Index: " + block.index);
            System.out.println("Timestamp: " + new Date(block.timestamp));
            System.out.println("Data: " + block.data);
            System.out.println("Previous Hash: " + block.previousHash.toString(16));
            System.out.println("Hash: " + block.hash.toString(16));
            System.out.println("Transactions: " + block.transactions);
            System.out.println();
        }
    }
    private class EndAuction extends Thread {
        Auction auction;
        String item;
        public EndAuction(Auction auction,String item) {
            this.auction = auction;
            this.item = item;
        }
        @Override
        public void run() {
            long endTime = System.currentTimeMillis() + 2 * 60 * 1000;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    auction.endAuction(node.getUsers(), node.getBlockchain());
                    node.getActiveAuctions().remove(item);
                }
            }, new Date(endTime));
        }
    }
}

