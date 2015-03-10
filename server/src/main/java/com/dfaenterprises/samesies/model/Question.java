package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class Question implements Storable {

    private Long id;
    private String q;
    private String category;

    public Question() {
    }

    public Question(Entity e) {
        this.id = e.getKey().getId();
        this.q = (String) e.getProperty("q");
        this.category = (String) e.getProperty("category");
    }

    public Question(String q) {
        this.q = q;
    }

    public Question(String q, String category) {
        this.q = q;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Entity toEntity() {
        Entity entity;
        if (id == null) {
            entity = new Entity("Question");
        } else {
            entity = new Entity("Question", id);
        }
        entity.setProperty("q", q);
        entity.setProperty("category", category);
        return entity;
    }
}
