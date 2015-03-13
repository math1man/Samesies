package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class Mode extends Storable {

    private String mode;
    private String description;

    public Mode() {
    }

    public Mode(Entity e) {
        super(e);
        this.mode = (String) e.getProperty("mode");
        this.description = (String) e.getProperty("description");
    }

    public Mode(String mode, String description) {
        this.mode = mode;
        this.description = description;
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
        Entity e = getEntity("Mode");
        e.setProperty("mode", mode);
        e.setUnindexedProperty("description", description);
        return e;
    }
}
