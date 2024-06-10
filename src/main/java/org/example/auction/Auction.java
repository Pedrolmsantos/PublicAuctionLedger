package org.example.auction;

import org.example.blockchain.Block;
import org.example.blockchain.Blockchain;
import org.example.crypto.CryptoUtils;
import org.example.network.Contact;
import org.example.network.Node;
import org.example.network.PublishBidPayload;
import org.example.network.RPC;

import java.math.BigInteger;
import java.security.*;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;

public class Auction {
    AuctionItem item;
    public String auctionDataSignature;
    private PublicKey sellerPublicKey;
    private Node node;

    public void setHighestBid(double highestBid) {
        item.setHighestBid(highestBid);
    }

    public AuctionItem getItem() {
        return item;
    }

    public void setItem(AuctionItem item) {
        this.item = item;
    }

    public void setHighestBidder(BigInteger highestBidder) {
        item.setHighestBidder(highestBidder);
    }

    public Auction(AuctionItem item, PrivateKey privateKey, PublicKey publicKey, Node node) throws Exception {
        this.item = item;
        this.node = node;
        this.sellerPublicKey = publicKey;
        this.auctionDataSignature = signAuctionData(privateKey);
    }

    private String signAuctionData(PrivateKey privateKey) throws Exception {
        String data = item.getSeller() + item.getItem() + item.getStartingPrice() + item.getEndTime();
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(data.getBytes("UTF-8"));
        byte[] signature = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    public boolean verifyAuctionData() throws Exception {
        String data = item.getSeller() + item.getItem() + item.getStartingPrice() + item.getEndTime();
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(sellerPublicKey);
        publicSignature.update(data.getBytes("UTF-8"));
        byte[] signatureBytes = Base64.getDecoder().decode(auctionDataSignature);
        return publicSignature.verify(signatureBytes);
    }

    public void endAuction(Map<BigInteger, User> users, Blockchain blockchain) {
        if (item.getHighestBidder() != null) {
            User highestBidderUser = users.get(item.getHighestBidder());
            //if (highestBidderUser != null) {
                String transactionData = item.getHighestBidder() + "-DEBIT-" + item.getHighestBid();
                Block newBlock = new Block(blockchain.chain.size(), transactionData, blockchain.getLatestBlock().hash);
                //new Transaction(CryptoUtils.applySha256(transactionData),highestBidderUser.getUserid(),highestBid,BigInteger.valueOf(System.currentTimeMillis()));
                newBlock.addTransaction(transactionData);
                blockchain.addBlock(newBlock);
                for(Contact c : node.getProtocol().getRoutingTable().getallcontacts()){
                    node.getRpc().send(c, new RPC.RPCMessage(RPC.SEND_DEBIT_REQUEST, node.getContact(), new PublishBidPayload(null, new Bid(item.getHighestBidder(), item.getHighestBid()))));
                }
                //node.broadcastblock(newBlock);
                System.out.println("Auction ended. " + item.getHighestBid() + " deducted from " +  item.getHighestBidder());
          //  }
        } else {
            System.out.println("Auction ended with no bids.");
        }
    }
}
