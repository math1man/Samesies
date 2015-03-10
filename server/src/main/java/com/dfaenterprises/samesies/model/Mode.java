package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class Mode implements Storable {

    private Long id;
    private String mode;
    private String description;

    public Mode() {
    }

    public Mode(Entity e) {
        this.id = e.getKey().getId();
        this.mode = (String) e.getProperty("mode");
        this.description = (String) e.getProperty("description");
    }

    public Mode(String mode, String description) {
        this.mode = mode;
        this.description = description;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Entity toEntity() {
        Entity entity;
        if (id == null) {
            entity = new Entity("Mode");
        } else {
            entity = new Entity("Mode", id);
        }
        entity.setProperty("mode", mode);
        entity.setUnindexedProperty("description", description);
        return entity;
    }
}
