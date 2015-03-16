package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * Generic superclass for things that go in the database.
 * Subclasses MUST implement both the no-args constructor and the Storable(Entity) contructor.
 * Concrete subclasses also must call getEntity() at the start of their toEntity() method with
 * the datastore name, and add parameters from there. Abstract subclasses should instead
 * override getEntity(), adding parameters there.
 *
 * @author Ari Weiland
 */
public abstract class Storable {

    private Long id;

    public Storable() {
    }

    public Storable(Entity e) {
        this.id = e.getKey().getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public abstract Entity toEntity();

    protected Entity getEntity(String name) {
        if (id == null) {
            return new Entity(name);
        } else {
            return new Entity(name, id);
        }
    }

}
