package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public abstract class Storable {

    private Long id;

    public Storable() {
    }

    public Storable(Entity e) {
        this.id = e.getKey().getId();
    }

    public Storable(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public abstract Entity toEntity();

    public final Entity getEntity(String name) {
        if (id == null) {
            return new Entity(name);
        } else {
            return new Entity(name, id);
        }
    }

}
