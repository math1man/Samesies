package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

import java.util.Date;

/**
 * Chats represent an instance of free communication between two users.
 *
 * Chats are tied to either an episode (created upon completion) or a friendship
 * (created when either friend messages the other).  Chats hold on to the ID of
 * the creating entity in the EOFID (Episode Or Friend ID) and also note whether
 * that entity was a episode or friend in the isEpisode method.  An episode chat
 * can be converted to a friend chat by updating those fields.
 *
 * Chats can be closed, which will prevent them from being retrieved by the
 * getChats method.  However, calling startChat on a chat via the EOFID will
 * reopen the chat.  Because episode IDs are generally not persisted, closing an
 * episode chat will prevent it from every being reopened, while a friend chat
 * can be reopened by either user messaging the other.
 *
 * @author Ari Weiland
 */
public class Chat extends Pairing {

    private Date startDate;
    private Long eofid; // episode or friend id
    private Boolean isEpisode; // or friend origin
    private Boolean isClosed;  // chats are closed if a user closes them
    private Date lastModified; // closed chats will not show up in getChats, but calling startChat will reopen them

    public Chat() {
    }

    public Chat(Entity e) {
        super(e);
        this.startDate = (Date) e.getProperty("startDate");
        this.eofid = (Long) e.getProperty("eofid");
        this.isEpisode = (Boolean) e.getProperty("isEpisode");
        this.isClosed = (Boolean) e.getProperty("isClosed");
        this.lastModified = (Date) e.getProperty("lastModified");
    }

    public Chat(Long eofid, Boolean isEpisode, Long uid1, Long uid2) {
        super(uid1, uid2);
        this.startDate = new Date();
        this.eofid = eofid;
        this.isEpisode = isEpisode;
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
        e.setProperty("isClosed", isClosed);
        e.setProperty("lastModified", lastModified);
        return e;
    }
}
