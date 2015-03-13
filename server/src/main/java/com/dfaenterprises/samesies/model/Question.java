package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class Question extends Storable {

    private String q;
    private String category;

    public Question() {
    }

    public Question(Entity e) {
        super(e);
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
        Entity e = getEntity("Question");
        e.setProperty("q", q);
        e.setProperty("category", category);
        return e;
    }
}
