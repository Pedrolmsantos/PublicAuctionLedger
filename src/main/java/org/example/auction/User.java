package org.example.auction;

import java.math.BigInteger;
import java.security.*;

public class User {
    public String name;
    public PublicKey publicKey;
    private PrivateKey privateKey;

    private BigInteger userid;

    public User(String name, BigInteger userid) throws Exception {
        this.name = name;
        this.userid = userid;
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getName() {
        return name;
    }
    public BigInteger getUserid(){
        return userid;
    }
}

