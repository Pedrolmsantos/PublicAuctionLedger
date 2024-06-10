package org.example.network;

import java.math.BigInteger;

public class StorePayload {
    private BigInteger key;
    private String value;
    public StorePayload(BigInteger key, String value) {
        this.key = key;
        this.value = value;
    }
    public BigInteger getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }
    public void setKey(BigInteger key) {
        this.key = key;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
