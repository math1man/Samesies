package com.dfaenterprises.samesies.model;

import com.dfaenterprises.samesies.EntityUtils;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;

import java.util.Date;
import java.util.List;

/**
 * @author Ari Weiland
 */
public class Episode extends Pairing {

    public static enum Status {
        MATCHING, UNMATCHED, IN_PROGRESS, ABANDONED, COMPLETE
    }

    private Date startDate;
    private Boolean isPersistent;
    private Settings settings;
    private Status status;
    private List<Long> qids;
    private List<String> answers1;
    private List<String> answers2;
    private Date lastModified;

    public Episode() {
    }

    public Episode(Entity e) {
        super(e);
        this.startDate = (Date) e.getProperty("startDate");
        this.isPersistent = (Boolean) e.getProperty("isPersistent");
        this.settings = new Settings(
                (String)  e.getProperty("mode"),
                (Boolean) e.getProperty("matchMale"),
                (Boolean) e.getProperty("matchFemale"),
                (Boolean) e.getProperty("matchOther"),
                (GeoPt)   e.getProperty("location"),
                (String)  e.getProperty("community"));
        this.status = Status.valueOf((String) e.getProperty("status"));
        this.qids = EntityUtils.entityToList(e.getProperty("qids"), 10, Long.class);
        this.answers1 = EntityUtils.entityToList(e.getProperty("answers1"), 10, String.class);
        this.answers2 = EntityUtils.entityToList(e.getProperty("answers2"), 10, String.class);
        this.lastModified = (Date) e.getProperty("lastModified");
    }

    /**
     * Create a new matching state, non-persistent Episode
     * (Random match mode)
     * @param uid1
     */
    public Episode(Long uid1, Settings settings) {
        super(uid1, null);
        this.startDate = new Date();
        this.isPersistent = false;
        this.settings = settings;
        this.status = Status.MATCHING;
        this.lastModified = startDate;
    }

    /**
     * Creates a new matching state, persistent Episode
     * (Challenge mode)
     * @param uid1
     * @param uid2
     */
    public Episode(Long uid1, Long uid2, Settings settings) {
        super(uid1, uid2);
        this.startDate = new Date();
        this.isPersistent = true;
        this.settings = settings;
        this.status = Status.MATCHING;
        this.lastModified = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Boolean getIsPersistent() {
        return isPersistent;
    }

    public void setIsPersistent(Boolean isPersistent) {
        this.isPersistent = isPersistent;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Long> getQids() {
        return qids;
    }

    public void setQids(List<Long> qids) {
        this.qids = qids;
    }

    public List<String> getAnswers1() {
        return answers1;
    }

    public void setAnswers1(List<String> answers1) {
        this.answers1 = answers1;
    }

    public List<String> getAnswers2() {
        return answers2;
    }

    public void setAnswers2(List<String> answers2) {
        this.answers2 = answers2;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public List<String> getAnswers(long uid) {
        if (isUid1(uid)) {
            return getAnswers1();
        } else {
            return getAnswers2();
        }
    }

    public void setAnswers(long uid, List<String> answers) {
        if (isUid1(uid)) {
            setAnswers1(answers);
        } else {
            setAnswers2(answers);
        }
    }

    public void modify() {
        lastModified = new Date();
    }

    public boolean isPersonal() {
        return settings.getMode().equals("Personal");
    }

    public Entity toEntity() {
        Entity e = getEntity("Episode");
        e.setProperty("startDate", startDate);
        e.setProperty("isPersistent", isPersistent);
        e.setProperty("mode", settings.getMode());
        e.setUnindexedProperty("matchMale", settings.getMatchMale());
        e.setUnindexedProperty("matchFemale", settings.getMatchFemale());
        e.setUnindexedProperty("matchOther", settings.getMatchOther());
        e.setUnindexedProperty("location", settings.getLocation());
        e.setUnindexedProperty("community", settings.getCommunity());
        e.setProperty("status", status.name());
        e.setUnindexedProperty("qids", EntityUtils.listToEntity(qids));
        e.setUnindexedProperty("answers1", EntityUtils.listToEntity(answers1));
        e.setUnindexedProperty("answers2", EntityUtils.listToEntity(answers2));
        e.setProperty("lastModified", lastModified);
        return e;
    }
}
