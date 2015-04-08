package com.dfaenterprises.samesies.model;

import com.dfaenterprises.samesies.EntityUtils;
import com.google.appengine.api.datastore.Entity;

import java.util.List;

/**
 * @author Ari Weiland
 */
public class Community extends Storable {

    public static enum Type {
        OPEN, EMAIL, PASSWORD, EXCLUSIVE
    }

    public static enum State {
        ACTIVE, PENDING, INACTIVE
    }

    // Database fields
    private String name;
    private String description;
    private Type type;
    private String utilityString;
    private State state;

    // Front end fields
    private List<User> users;

    // **v1.0.0** TODO: remove location, needed for compatibility
    private String location;

    public Community() {
    }

    public Community(Entity e) {
        super(e);
        this.name = (String) e.getProperty("name");
        this.location = name;
        this.description = (String) e.getProperty("description");
        this.type = EntityUtils.getEnumProp(e, "type", Type.class);
        this.utilityString = (String) e.getProperty("utilityString");
        this.state = EntityUtils.getEnumProp(e, "state", State.class);
    }

    public Community(String name, String description) {
        this(name, description, Type.OPEN, null);
    }

    public Community(String name, String description, Type type, String utilityString) {
        this.name = name;
        this.location = name;
        setDescription(description);
        this.type = type;
        this.utilityString = utilityString;
        this.state = State.ACTIVE;
    }

    public Community(String name, List<User> users) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getUtilityString() {
        return utilityString;
    }

    public void setUtilityString(String utilityString) {
        this.utilityString = utilityString;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public Entity toEntity() {
        Entity e = getEntity("Community");
        e.setProperty("name", name);
        e.setUnindexedProperty("description", description);
        e.setUnindexedProperty("type", type.name());
        e.setUnindexedProperty("utilityString", utilityString);
        e.setProperty("state", state.name());
        return e;
    }
}
