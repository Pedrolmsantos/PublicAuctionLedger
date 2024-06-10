package org.example.auction;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.network.Node;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;

public class AuctionItem {
    public BigInteger seller;
    public String item;
    public double startingPrice;
    public double highestBid;
    public BigInteger highestBidder;
    public long endTime;
    public void setHighestBid(double highestBid) {
        this.highestBid = highestBid;
    }

    public void setHighestBidder(BigInteger highestBidder) {
        this.highestBidder = highestBidder;
    }

    public AuctionItem(@JsonProperty("seller") BigInteger seller,@JsonProperty("item") String item,@JsonProperty("startingPrice") double startingPrice,@JsonProperty("endTime") long endTime) throws Exception {
        this.seller = seller;
        this.item = item;
        this.startingPrice = startingPrice;
        this.highestBid = startingPrice;
        this.highestBidder = null;
        this.endTime = endTime;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public double getHighestBid() {
        return highestBid;
    }

    public BigInteger getHighestBidder() {
        return highestBidder;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public BigInteger getSeller() {
        return seller;
    }

    public void setSeller(BigInteger seller) {
        this.seller = seller;
    }
}
