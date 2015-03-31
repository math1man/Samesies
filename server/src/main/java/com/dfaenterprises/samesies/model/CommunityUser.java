package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class CommunityUser extends Storable {

    private Long cid;
    private Long uid;
    private Boolean isValidated;

    public CommunityUser() {
    }

    public CommunityUser(Entity e) {
        super(e);
        this.cid = (Long) e.getProperty("cid");
        this.uid = (Long) e.getProperty("uid");
        this.isValidated = (Boolean) e.getProperty("isValidated");
    }

    public CommunityUser(Long cid, Long uid) {
        this.cid = cid;
        this.uid = uid;
        this.isValidated = false;
    }

    public CommunityUser(Long cid, Long uid, Boolean isValidated) {
        this.cid = cid;
        this.uid = uid;
        this.isValidated = isValidated;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Boolean getIsValidated() {
        return isValidated;
    }

    public void setIsValidated(Boolean isValidated) {
        this.isValidated = isValidated;
    }

    @Override
    public Entity toEntity() {
        Entity e = getEntity("CommunityUser");
        e.setProperty("cid", cid);
        e.setProperty("uid", uid);
        e.setProperty("isValidated", isValidated);
        return e;
    }
}
