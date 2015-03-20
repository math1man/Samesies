package com.dfaenterprises.samesies.model;

import java.util.List;

/**
 * @author Ari Weiland
 */
public class UserGroup {

    private String name;
    private List<User> users;

    public UserGroup() {
    }

    public UserGroup(String name, List<User> users) {
        this.name = name;
        this.users = users;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
