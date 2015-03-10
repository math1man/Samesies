package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class Answer implements Storable {

    private Long id;
    private Long qid;
    private Long uid;
    private String answer;

    public Answer() {
    }

    public Answer(Entity e) {
        this.id = e.getKey().getId();
        this.qid = (Long) e.getProperty("qid");
        this.uid = (Long) e.getProperty("uid");
        this.answer = (String) e.getProperty("answer");
    }

    public Answer(Long qid, Long uid, String answer) {
        this.qid = qid;
        this.uid = uid;
        this.answer = answer;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
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
        Entity entity;
        if (id == null) {
            entity = new Entity("Answer");
        } else {
            entity = new Entity("Answer", id);
        }
        entity.setProperty("qid", qid);
        entity.setProperty("uid", uid);
        entity.setUnindexedProperty("answer", answer);
        return entity;
    }
}
