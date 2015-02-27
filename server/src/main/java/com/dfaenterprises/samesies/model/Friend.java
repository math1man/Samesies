package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class Friend implements Storable {

    public static enum Status {
        PENDING, ACCEPTED, DELETED_1, DELETED_2;

        public User.Relation getRelation() {
            return this == ACCEPTED ? User.Relation.FRIEND : User.Relation.STRANGER;
        }

        public boolean isDeleted() {
            return this == DELETED_1 || this == DELETED_2;
        }
    }

    private Long id;
    private Long uid1;
    private Long uid2;
    private User user;
    private Status status;

    public Friend() {
    }

    public Friend(Entity e) {
        this.id = e.getKey().getId();
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

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
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
        Entity entity;
        if (id == null) {
            entity = new Entity("Friend");
        } else {
            entity = new Entity("Friend", id);
        }
        entity.setProperty("uid1", uid1);
        entity.setProperty("uid2", uid2);
        entity.setProperty("status", status.name());
        return entity;
    }
}
