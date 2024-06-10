package org.example.auction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;

public class Bid {
    public BigInteger userid;
    public double amount;

    public Bid(@JsonProperty("userid") BigInteger userid,@JsonProperty("amount") double amount) {
        this.userid = userid;
        this.amount = amount;
    }

    public BigInteger getUserid() {
        return userid;
    }

    public void setUserid(BigInteger userid) {
        this.userid = userid;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
