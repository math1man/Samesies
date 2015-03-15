package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

import java.util.Date;

/**
 * @author Ari Weiland
 */
public class Chat extends Storable {

    private Date startDate;
    private Long eofid; // episode or friend id
    private Boolean isEpisode; // or friend origin
    private Long uid1;
    private Long uid2;
    private Boolean isClosed;
    private Date lastModified;

    public Chat() {
    }

    public Chat(Entity e) {
        super(e);
        this.startDate = (Date) e.getProperty("startDate");
        this.eofid = (Long) e.getProperty("eofid");
        this.isEpisode = (Boolean) e.getProperty("isEpisode");
        this.uid1 = (Long) e.getProperty("uid1");
        this.uid2 = (Long) e.getProperty("uid2");
        this.isClosed = (Boolean) e.getProperty("isClosed");
        this.lastModified = (Date) e.getProperty("lastModified");
    }

    public Chat(Long eofid, Boolean isEpisode, Long uid1, Long uid2) {
        this.startDate = new Date();
        this.eofid = eofid;
        this.isEpisode = isEpisode;
        this.uid1 = uid1;
        this.uid2 = uid2;
        this.isClosed = false;
        this.lastModified = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Long getEofid() {
        return eofid;
    }

    public void setEofid(Long eofid) {
        this.eofid = eofid;
    }

    public Boolean getIsEpisode() {
        return isEpisode;
    }

    public void setIsEpisode(Boolean isEpisode) {
        this.isEpisode = isEpisode;
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

    public Boolean getIsClosed() {
        return isClosed;
    }

    public void setIsClosed(Boolean isClosed) {
        this.isClosed = isClosed;
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
        e.setProperty("eofid", eofid);
        e.setProperty("isEpisode", isEpisode);
        e.setProperty("uid1", uid1);
        e.setProperty("uid2", uid2);
        e.setProperty("isClosed", isClosed);
        e.setProperty("lastModified", lastModified);
        return e;
    }
}
