package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class Push extends Storable {

    private Long uid;
    private String type;
    private String deviceToken;

    public Push() {
    }

    public Push(Entity e) {
        super(e);
        this.uid = (Long) e.getProperty("uid");
        this.type = (String) e.getProperty("type");
        this.deviceToken = (String) e.getProperty("deviceToken");
    }

    public Push(Long uid, String type, String deviceToken) {
        this.uid = uid;
        this.type = type;
        this.deviceToken = deviceToken;
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

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    @Override
    public Entity toEntity() {
        Entity e = getEntity("Push");
        e.setProperty("uid", uid);
        e.setProperty("type", type);
        e.setProperty("deviceToken", deviceToken);
        return e;
    }
}
