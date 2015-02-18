package com.dfaenterprises.samesies.model;

import java.util.List;

/**
 * @author Ari Weiland
 */
public class Community {

    private String location;
    private List<User> users;

    public Community() {
    }

    public Community(String location, List<User> users) {
        this.location = location;
        this.users = users;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
