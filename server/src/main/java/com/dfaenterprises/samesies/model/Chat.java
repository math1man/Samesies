package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

import java.util.Date;

/**
 * @author Ari Weiland
 */
public class Chat extends Storable {

    private Date startDate;
    private Long uid1;
    private Long uid2;
    private Date lastModified;

    public Chat() {
    }

    public Chat(Entity e) {
        super(e);
        this.startDate = (Date) e.getProperty("startDate");
        this.uid1 = (Long) e.getProperty("uid1");
        this.uid2 = (Long) e.getProperty("uid2");
        this.lastModified = (Date) e.getProperty("lastModified");
    }

    public Chat(Long uid1, Long uid2) {
        this.startDate = new Date();
        this.uid1 = uid1;
        this.uid2 = uid2;
        this.lastModified = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
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

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public Entity toEntity() {
        Entity e = getEntity("Chat");
        e.setProperty("startDate", startDate);
        e.setProperty("uid1", uid1);
        e.setProperty("uid2", uid2);
        e.setProperty("lastModified", lastModified);
        return e;
    }
}
