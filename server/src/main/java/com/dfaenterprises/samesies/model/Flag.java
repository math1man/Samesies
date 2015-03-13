package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

import java.util.Date;

/**
 * @author Ari Weiland
 */
public class Flag extends Storable {

    private Long flaggedId;
    private Long flaggerId;
    private String reason;
    private Date date;
    private Boolean isAcknowledged;

    public Flag() {
    }

    public Flag(Entity e) {
        super(e);
        this.flaggedId = (Long) e.getProperty("flaggedId");
        this.flaggerId = (Long) e.getProperty("flaggerId");
        this.reason = (String) e.getProperty("reason");
        this.date = (Date) e.getProperty("date");
        this.isAcknowledged = (Boolean) e.getProperty("isAcknowledged");
    }

    public Flag(Long flaggedId, Long flaggerId, String reason) {
        this.flaggedId = flaggedId;
        this.flaggerId = flaggerId;
        this.reason = reason;
        this.date = new Date();
        this.isAcknowledged = false;
    }

    public Long getFlaggedId() {
        return flaggedId;
    }

    public void setFlaggedId(Long flaggedId) {
        this.flaggedId = flaggedId;
    }

    public Long getFlaggerId() {
        return flaggerId;
    }

    public void setFlaggerId(Long flaggerId) {
        this.flaggerId = flaggerId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public Entity toEntity() {
        Entity e = getEntity("Flag");
        e.setProperty("flaggedId", flaggedId);
        e.setProperty("flaggerId", flaggerId);
        e.setUnindexedProperty("reason", reason);
        e.setProperty("date", date);
        e.setProperty("isAcknowledged", isAcknowledged);
        return e;
    }
}
