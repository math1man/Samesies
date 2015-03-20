package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.GeoPt;

import java.util.List;

/**
 * @author Ari Weiland
 */
public class Community {

    private String name;
    private GeoPt location;
    private List<User> users;

    public Community() {
    }

    public Community(String name, List<User> users) {
        this.name = name;
        this.location = null;
        this.users = users;
    }

    public Community(GeoPt location, List<User> users) {
        this.name = "Near By";
        this.location = location;
        this.users = users;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPt getLocation() {
        return location;
    }

    public void setLocation(GeoPt location) {
        this.location = location;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
