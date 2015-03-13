package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class Friend extends Storable {

    public static enum Status {
        PENDING, ACCEPTED, DELETED_1, DELETED_2;

        public User.Relation getRelation() {
            return this == ACCEPTED ? User.Relation.FRIEND : User.Relation.STRANGER;
        }

        public boolean isDeleted() {
            return this == DELETED_1 || this == DELETED_2;
        }
    }

    private Long uid1;
    private Long uid2;
    private User user;
    private Status status;

    public Friend() {
    }

    public Friend(Entity e) {
        super(e);
        this.uid1 = (Long) e.getProperty("uid1");
        this.uid2 = (Long) e.getProperty("uid2");
        this.status = Status.valueOf((String) e.getProperty("status"));
    }

    public Friend(Long uid1, Long uid2) {
        this(uid1, uid2, Status.PENDING);
    }

    public Friend(Long uid1, Long uid2, Status status) {
        this.uid1 = uid1;
        this.uid2 = uid2;
        this.status = status;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public Entity toEntity() {
        Entity e = getEntity("Friend");
        e.setProperty("uid1", uid1);
        e.setProperty("uid2", uid2);
        e.setProperty("status", status.name());
        return e;
    }
}
