package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class Answer extends Storable {

    private Long qid;
    private Long uid;
    private String answer;

    public Answer() {
    }

    public Answer(Entity e) {
        super(e);
        this.qid = (Long) e.getProperty("qid");
        this.uid = (Long) e.getProperty("uid");
        this.answer = (String) e.getProperty("answer");
    }

    public Answer(Long qid, Long uid, String answer) {
        this.qid = qid;
        this.uid = uid;
        this.answer = answer;
    }

    public Long getQid() {
        return qid;
    }

    public void setQid(Long qid) {
        this.qid = qid;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public Entity toEntity() {
        Entity e = getEntity("Answer");
        e.setProperty("qid", qid);
        e.setProperty("uid", uid);
        e.setUnindexedProperty("answer", answer);
        return e;
    }
}
