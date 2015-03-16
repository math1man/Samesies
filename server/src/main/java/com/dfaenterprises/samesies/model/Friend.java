package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class Friend extends Pairing {

    public static enum Status {
        PENDING, ACCEPTED, DELETED_1, DELETED_2;

        public User.Relation getRelation() {
            return this == ACCEPTED ? User.Relation.FRIEND : User.Relation.STRANGER;
        }

        public boolean isDeleted() {
            return this == DELETED_1 || this == DELETED_2;
        }
    }

    private Status status;

    public Friend() {
    }

    public Friend(Entity e) {
        super(e);
        this.status = Status.valueOf((String) e.getProperty("status"));
    }

    public Friend(Long uid1, Long uid2) {
        this(uid1, uid2, Status.PENDING);
    }

    public Friend(Long uid1, Long uid2, Status status) {
        super(uid1, uid2);
        this.status = status;
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
        e.setProperty("status", status.name());
        return e;
    }
}
