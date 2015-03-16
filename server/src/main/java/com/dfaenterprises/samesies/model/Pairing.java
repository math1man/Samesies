package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public abstract class Pairing extends Storable {

    private Long uid1;
    private Long uid2;

    private User user;

    public Pairing() {
    }

    public Pairing(Entity e) {
        super(e);
        this.uid1 = (Long) e.getProperty("uid1");
        this.uid2 = (Long) e.getProperty("uid2");
    }

    public Pairing(Long uid1, Long uid2) {
        this.uid1 = uid1;
        this.uid2 = uid2;
    }

    public Long getUid1() {
        return uid1;
    }

    public void setUid1(Long uid1) {
        this.uid1 = uid1;
    }

    public Long getUid2() {
        return uid2;
    }

    public void setUid2(Long uid2) {
        this.uid2 = uid2;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isUid1(long uid) {
        return uid == uid1;
    }

    public Long getOtherUid(long uid) {
        if (uid == uid1) {
            return uid2;
        } else if (uid == uid2) {
            return uid1;
        } else {
            return null;
        }
    }

    @Override
    protected Entity getEntity(String name) {
        Entity e = super.getEntity(name);
        e.setProperty("uid1", uid1);
        e.setProperty("uid2", uid2);
        return e;
    }
}
