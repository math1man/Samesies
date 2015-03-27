package com.dfaenterprises.samesies.model;

import java.util.List;

/**
 * @author Ari Weiland
 */
public class UserGroup {

    private String name;
    // **Low-Priority** TODO: remove location eventually, needed for compatibility
    private String location;
    private List<User> users;

    public UserGroup() {
    }

    public UserGroup(String name, List<User> users) {
        this.name = name;
        this.location = name;
        this.users = users;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
