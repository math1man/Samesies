package com.dfaenterprises.samesies.model;

import com.dfaenterprises.samesies.EntityUtils;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;

/**
 * @author Ari Weiland
 */
public class Community extends Storable {

    public static enum Validation {
        NONE, EMAIL, PASSWORD
    }

    private String name;
    private Text description;
    private Validation validation;
    private String validationString;

    public Community() {
    }

    public Community(Entity e) {
        super(e);
        this.name = (String) e.getProperty("name");
        this.description = (Text) e.getProperty("description");
        this.validation = EntityUtils.getEnumProp(e, "validation", Validation.class);
        this.validationString = (String) e.getProperty("validationString");
    }

    public Community(String name, String description) {
        this(name, description, Validation.NONE, null);
    }

    public Community(String name, String description, Validation validation, String validationString) {
        this.name = name;
        setDescription(description);
        this.validation = validation;
        this.validationString = validationString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        if (description == null) {
            return null;
        } else {
            return description.getValue();
        }
    }

    public void setDescription(String description) {
        if (description == null) {
            this.description = null;
        } else {
            this.description = new Text(description);
        }
    }

    public Validation getValidation() {
        return validation;
    }

    public void setValidation(Validation validation) {
        this.validation = validation;
    }

    public String getValidationString() {
        return validationString;
    }

    public void setValidationString(String validationString) {
        this.validationString = validationString;
    }

    @Override
    public Entity toEntity() {
        Entity e = getEntity("Community");
        e.setProperty("name", name);
        e.setUnindexedProperty("description", description);
        e.setUnindexedProperty("validation", validation.name());
        e.setUnindexedProperty("validationString", validationString);
        return e;
    }
}
