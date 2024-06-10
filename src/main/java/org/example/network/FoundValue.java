package org.example.network;

import java.util.List;

public class FoundValue {
    public List<Contact> closestContacts;
    public StorePayload value;

    public FoundValue(List<Contact> closestContacts, StorePayload value) {
        this.closestContacts = closestContacts;
        this.value = value;
    }
    public List<Contact> getClosestContacts() {
        return closestContacts;
    }
    public void setClosestContacts(List<Contact> closestContacts) {
        this.closestContacts = closestContacts;
    }
    public StorePayload getValue() {
        return value;
    }
    public void setValue(StorePayload value) {
        this.value = value;
    }
}
