package org.example.blockchain;

import java.security.MessageDigest;
import java.util.*;

import org.example.crypto.CryptoUtils;

import static org.example.crypto.CryptoUtils.applySha256;

import java.util.*;
import java.math.BigInteger;

public class Block {
    public int index;
    public long timestamp;
    public String data;
    public BigInteger previousHash;
    public BigInteger hash;
    public int nonce;
    public List<String> transactions;

    public Block(int index, String data, BigInteger previousHash) {
        this.index = index;
        this.timestamp = new Date().getTime();
        this.data = data;
        this.previousHash = previousHash;
        this.hash = calculateHash();
        this.transactions = new ArrayList<>();
    }

    public BigInteger calculateHash() {
        try {
            String input = index + Long.toString(timestamp) + data + previousHash.toString() + nonce;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());
            return new BigInteger(1, hashBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.toString(16).substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash.toString(16));
    }

    public void addTransaction(String transaction) {
        transactions.add(transaction);
    }
}






