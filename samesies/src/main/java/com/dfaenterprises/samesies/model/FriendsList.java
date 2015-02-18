package com.dfaenterprises.samesies.model;

import java.util.List;

/**
 * @author Ari Weiland
 */
public class FriendsList {

    private long uid;
    private List<User> friends;

    public FriendsList() {
    }

    public FriendsList(long uid, List<User> friends) {
        this.uid = uid;
        this.friends = friends;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public List<User> getFriends() {
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }
}
