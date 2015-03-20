package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class CommunityUser extends Storable {

    private String community;
    private Long uid;
    private Boolean isValidated;

    public CommunityUser() {
    }

    public CommunityUser(Entity e) {
        super(e);
        this.community = (String) e.getProperty("community");
        this.uid = (Long) e.getProperty("uid");
        this.isValidated = (Boolean) e.getProperty("isValidated");
    }

    public CommunityUser(String community, Long uid) {
        this.community = community;
        this.uid = uid;
        this.isValidated = false;
    }

    @Override
    public Entity toEntity() {
        Entity e = getEntity("CommunityUser");
        e.setProperty("community", community);
        e.setProperty("uid", uid);
        e.setProperty("isValidated", isValidated);
        return e;
    }
}
