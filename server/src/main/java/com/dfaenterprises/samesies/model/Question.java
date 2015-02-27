package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class Question implements Storable {

    private Long id;
    private String q;
    private String a;
    private String category;

    public Question() {
    }

    public Question(Entity e) {
        this.id = e.getKey().getId();
        this.q = (String) e.getProperty("q");
//        this.a = (String) e.getProperty("a");
        this.category = (String) e.getProperty("category");
    }

    public Question(String q) {
        this.q = q;
    }

    public Question(String q, String a) {
        this.q = q;
        this.a = a;
    }

    public Question(String q, String a, String category) {
        this.q = q;
        this.a = a;
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

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Entity toEntity() {
        Entity entity = new Entity("Question");
        entity.setProperty("q", q);
//        entity.setUnindexedProperty("a", a);
        entity.setProperty("category", category);
        return entity;
    }
}
