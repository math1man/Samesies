package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

import java.util.Date;

/**
 * @author Ari Weiland
 */
public class Chat implements Storable {

    private Long id;
    private Date startDate;
    private Long uid1;
    private Long uid2;
    private Date lastModified;

    public Chat() {
    }

    public Chat(Entity entity) {
        this.id = entity.getKey().getId();
        this.startDate = (Date) entity.getProperty("startDate");
        this.uid1 = (Long) entity.getProperty("uid1");
        this.uid2 = (Long) entity.getProperty("uid2");
        this.lastModified = (Date) entity.getProperty("lastModified");
    }

    public Chat(Long uid1, Long uid2) {
        this.startDate = new Date();
        this.uid1 = uid1;
        this.uid2 = uid2;
        this.lastModified = startDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void modify() {
        setLastModified(new Date());
    }

    @Override
    public Entity toEntity() {
        Entity entity;
        if (id == null) {
            entity = new Entity("Chat");
        } else {
            entity = new Entity("Chat", id);
        }
        entity.setProperty("startDate", startDate);
        entity.setProperty("uid1", uid1);
        entity.setProperty("uid2", uid2);
        entity.setProperty("lastModified", lastModified);
        return entity;
    }
}
