package org.example.blockchain;

import java.math.BigInteger;
import java.util.*;

public class Blockchain {
    public static final double INITIAL_BALANCE = 1000.0;
    public List<Block> chain;
    public int difficulty;

    public Blockchain(int difficulty) {
        this.chain = new ArrayList<>();
        this.difficulty = difficulty;
        chain.add(createGenesisBlock());
    }

    private Block createGenesisBlock() {
        return new Block(0, "Genesis Block", BigInteger.ZERO);
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        chain.add(newBlock);
    }

    public double getUserBalance(BigInteger userName) {
        double balance = INITIAL_BALANCE;
        for (Block block : chain) {
            for (String transaction : block.transactions) {
                if(transaction.contains("DEBIT") || transaction.contains("CREDIT")){
                    String[] parts = transaction.split("-");
                //    if (parts.length == 4) {
                        BigInteger user = new BigInteger(parts[0]);
                        String action = parts[1];
                        double amount = Double.parseDouble(parts[2]);
                        if (user.equals(userName)) {
                            if (action.equals("DEBIT")){
                                balance -= amount;
                            } else if (action.equals("CREDIT")){
                                balance += amount;
                            }
                        }
                   // }
                }
            }
        }
        return balance;
    }
}
