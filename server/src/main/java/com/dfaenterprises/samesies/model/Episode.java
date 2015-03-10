package com.dfaenterprises.samesies.model;

import com.dfaenterprises.samesies.EntityUtils;
import com.google.appengine.api.datastore.Entity;

import java.util.Date;
import java.util.List;

/**
 * @author Ari Weiland
 */
public class Episode implements Storable {

    public static enum Status {
        MATCHING, UNMATCHED, IN_PROGRESS, ABANDONED, COMPLETE
    }

    private Long id;
    private Date startDate;
    private Boolean isPersistent;
    private Settings settings;
    private Status status;
    private Long uid1;
    private Long uid2;
    private List<Long> qids;
    private List<String> answers1;
    private List<String> answers2;

    public Episode() {
    }

    public Episode(Entity e) {
        this.id = e.getKey().getId();
        this.startDate = (Date) e.getProperty("startDate");
        this.isPersistent = (Boolean) e.getProperty("isPersistent");
        this.settings = new Settings(
                (String) e.getProperty("mode"),
                (Boolean) e.getProperty("matchMale"),
                (Boolean) e.getProperty("matchFemale"),
                (Boolean) e.getProperty("matchOther"));
        this.status = Status.valueOf((String) e.getProperty("status"));
        this.uid1 = (Long) e.getProperty("uid1");
        this.uid2 = (Long) e.getProperty("uid2");
        this.qids = EntityUtils.entityToList(e.getProperty("qids"), 10, Long.class);
        this.answers1 = EntityUtils.entityToList(e.getProperty("answers1"), 10, String.class);
        this.answers2 = EntityUtils.entityToList(e.getProperty("answers2"), 10, String.class);
    }

    /**
     * Create a new matching state, non-persistent Episode
     * (Random match mode)
     * @param uid1
     */
    public Episode(Long uid1, Settings settings) {
        this.startDate = new Date();
        this.isPersistent = false;
        this.settings = settings;
        this.status = Status.MATCHING;
        this.uid1 = uid1;
    }

    /**
     * Creates a new matching state, persistent Episode
     * (Challenge mode)
     * @param uid1
     * @param uid2
     */
    public Episode(Long uid1, Long uid2, Settings settings) {
        this.startDate = new Date();
        this.isPersistent = true;
        this.settings = settings;
        this.status = Status.MATCHING;
        this.uid1 = uid1;
        this.uid2 = uid2;
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

    public List<String> getAnswers(boolean is1) {
        if (is1) {
            return getAnswers1();
        } else {
            return getAnswers2();
        }
    }

    public void setAnswers(boolean is1, List<String> answers) {
        if (is1) {
            setAnswers1(answers);
        } else {
            setAnswers2(answers);
        }
    }

    public boolean isPersonal() {
        return settings.getMode().equals("Personal");
    }

    public Entity toEntity() {
        Entity e;
        if (id == null) {
            e = new Entity("Episode");
        } else {
            e = new Entity("Episode", id);
        }
        e.setProperty("startDate", startDate);
        e.setProperty("isPersistent", isPersistent);
        e.setProperty("mode", settings.getMode());
        e.setUnindexedProperty("matchMale", settings.getMatchMale());
        e.setUnindexedProperty("matchFemale", settings.getMatchFemale());
        e.setUnindexedProperty("matchOther", settings.getMatchOther());
        e.setProperty("status", status.name());
        e.setProperty("uid1", uid1);
        e.setProperty("uid2", uid2);
        e.setProperty("qids", EntityUtils.listToEntity(qids));
        e.setProperty("answers1", EntityUtils.listToEntity(answers1));
        e.setProperty("answers2", EntityUtils.listToEntity(answers2));
        return e;
    }
}
