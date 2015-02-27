package com.dfaenterprises.samesies.model;

import com.dfaenterprises.samesies.EntityUtils;
import com.google.appengine.api.datastore.Entity;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ari Weiland
 */
public class User implements Storable {

    public static enum Relation {
        STRANGER, FRIEND, SELF, ADMIN
    }

    private Long id;
    private String email;
    private String password;
    private String location;
    private String alias;
    // TODO: profile picture
    private String name;
    private Integer age;
    private String gender;
    // TODO: interested in?
    private String aboutMe;
    private List<String> questions;

    private Relation relation;

    public User() {
    }

    public User(Entity entity) {
        this(entity, Relation.ADMIN);
    }

    public User(Entity entity, Relation relation) {
        int ordinal = relation.ordinal();
        // public
        this.id = entity.getKey().getId();
        this.location = (String) entity.getProperty("location");
        this.alias = (String) entity.getProperty("alias");
        this.relation = relation;
        // private
        if (ordinal >= Relation.SELF.ordinal()) {
            this.email = (String) entity.getProperty("email");
            this.password = (String) entity.getProperty("password");
        }
        // protected
        if (ordinal >= Relation.FRIEND.ordinal()) {
            this.name = (String) entity.getProperty("name");
            // THIS FUCKING CODE SUCKS
            Object o = entity.getProperty("age");
            if (o == null) {
                this.age = null;
            } else {
                this.age = ((Long) o).intValue();
            }
            this.gender = (String) entity.getProperty("gender");
            this.aboutMe = (String) entity.getProperty("aboutMe");
            this.questions = EntityUtils.entityToList(entity.getProperty("questions"), 5, String.class);
        }
    }

    public User(String email, String password, String location, String alias,
                String name, Integer age, String gender, String aboutMe) {
        this.email = email;
        this.password = password;
        this.location = location;
        this.alias = alias;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.aboutMe = aboutMe;
        this.questions = blankQuestions();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public Entity toEntity() {
        Entity entity;
        if (id == null) {
            entity = new Entity("User");
        } else {
            entity = new Entity("User", id);
        }
        entity.setProperty("email", email);
        entity.setUnindexedProperty("password", password);
        entity.setProperty("location", location);
        entity.setProperty("alias", alias);
        entity.setProperty("name", name);
        entity.setProperty("age", age);
        entity.setProperty("gender", gender);
        entity.setUnindexedProperty("aboutMe", aboutMe);
        entity.setUnindexedProperty("questions", EntityUtils.listToEntity(questions));
        return entity;
    }

    public void setDefaultAlias() {
        if (email != null) {
            setAlias(getAlias(email));
        }
    }

    public void setBlankQuestions() {
        questions = blankQuestions();
    }

    public static String getAlias(String email) {
        return email.substring(0, email.indexOf('@'));
    }

    private static List<String> blankQuestions() {
        return Arrays.asList("", "", "", "", "");
    }
}
