package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class Push extends Storable {

    private Long uid;
    private String type;
    private String pushId;

    public Push() {
    }

    public Push(Entity e) {
        super(e);
        this.uid = (Long) e.getProperty("uid");
        this.type = (String) e.getProperty("type");
        this.pushId = (String) e.getProperty("pushId");
    }

    public Push(Long uid, String type, String pushId) {
        this.uid = uid;
        this.type = type;
        this.pushId = pushId;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    @Override
    public Entity toEntity() {
        Entity e = getEntity("Push");
        e.setProperty("uid", uid);
        e.setUnindexedProperty("type", type);
        e.setUnindexedProperty("pushId", pushId);
        return e;
    }
}
