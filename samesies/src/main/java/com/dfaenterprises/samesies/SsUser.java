package com.dfaenterprises.samesies;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.repackaged.com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ari Weiland
 */
public class SsUser implements Serializable {

    private Long id;
    private String email;
    private String password;
    private String location;
    private String alias;
    private String name;
    private Integer age;
    private String gender;
    private String aboutMe;
    private String[] questions;

    public SsUser() {

    }

    public SsUser(Entity entity) {
        this(
                entity.getKey().getId(),
                (String) entity.getProperty("email"),
                (String) entity.getProperty("password"),
                (String) entity.getProperty("location"),
                (String) entity.getProperty("alias"),
                (String) entity.getProperty("name"),
                (Integer) entity.getProperty("age"),
                (String) entity.getProperty("gender"),
                (String) entity.getProperty("aboutMe"),
                (String[]) entity.getProperty("questions")
        );
    }

    public SsUser(String email, String password, String location) {
        this((Long) null, email, password, location);
    }

    public SsUser(String email, String password, String location, String alias) {
        this(null, email, password, location, alias);
    }

    public SsUser(Long id, String email, String password, String location) {
        this(id, email, password, location, email.substring(0, email.indexOf('@')));
    }

    public SsUser(Long id, String email, String password, String location, String alias) {
        this(id, email, password, location, alias, "", null, "");
    }

    public SsUser(Long id, String email, String password, String location, String alias,
                  String name, Integer age, String gender) {
        this(id, email, password, location, alias, name, age, gender, "");
    }

    public SsUser(Long id, String email, String password, String location, String alias,
                  String name, Integer age, String gender, String aboutMe) {
        this(id, email, password, location, alias, name, age, gender, aboutMe, new String[]{"", "", "", "", ""});
    }

    public SsUser(Long id, String email, String password, String location, String alias,
                  String name, Integer age, String gender, String aboutMe, String[] questions) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.location = location;
        this.alias = alias;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.aboutMe = aboutMe;
        this.questions = questions;
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

    public String[] getQuestions() {
        return questions;
    }

    public void setQuestions(String[] questions) {
        this.questions = questions;
    }

    public static Entity makeUser(String email, String password, String location, String alias,
                                  String name, Integer age, String gender, String aboutMe, String[] questions) {
        Entity entity = makeUser(email, password, location, alias, name, age, gender, aboutMe);
        entity.setUnindexedProperty("questions", makeQuestions(questions));
        return entity;
    }

    public static Entity makeUser(String email, String password, String location, String alias,
                                  String name, Integer age, String gender, String aboutMe) {
        Entity entity = makeUser(email, password, location, alias, name, age, gender);
        entity.setUnindexedProperty("aboutMe", aboutMe);
        return entity;
    }

    public static Entity makeUser(String email, String password, String location, String alias,
                                  String name, Integer age, String gender) {
        Entity entity = makeUser(email, password, location, alias);
        entity.setProperty("name", name);
        entity.setProperty("age", age);
        entity.setProperty("gender", gender);
        return entity;
    }

    public static Entity makeUser(String email, String password, String location, String alias) {
        Entity entity = makeUser(email, password, location);
        if (alias != null && !alias.isEmpty()) {
            entity.setProperty("alias", alias);
        }
        return entity;
    }

    public static Entity makeUser(String email, String password, String location) {
        if (email == null || email.isEmpty()) {
            throw new NullPointerException("Must specify an email!");
        }
        if (password == null || password.isEmpty()) {
            throw new NullPointerException("Must specify a password!");
        }
        if (location == null || location.isEmpty()) {
            throw new NullPointerException("Must specify a location!");
        }
        Entity entity = new Entity("User");
        entity.setProperty("email", email);
        entity.setUnindexedProperty("password", password);
        entity.setProperty("location", location);
        entity.setProperty("alias", getAlias(email));
        entity.setProperty("name", "");
        entity.setProperty("age", null);
        entity.setProperty("gender", "");
        entity.setUnindexedProperty("aboutMe", "");
        entity.setUnindexedProperty("questions", makeQuestions(null));
        return entity;
    }

    public static String getAlias(String email) {
        return email.substring(0, email.indexOf('@'));
    }

    /**
     * Creates an EmbeddedEntity representing the questions.
     * If null is specified, or if less than 5 questions are
     * given, the questions will all be set to the empty string.
     * If more than 5 questions are given, only the first 5
     * questions will be used.
     * @param questions
     * @return
     */
    public static EmbeddedEntity makeQuestions(String[] questions) {
        if (questions == null || questions.length < 5) {
            questions = new String[]{"", "", "", "", ""};
        }
        EmbeddedEntity questionsEntity = new EmbeddedEntity();
        questionsEntity.setProperty("length", 5);
        for (int i=0; i<5; i++) {
            questionsEntity.setProperty("" + i, questions[i]);
        }
        return questionsEntity;
    }

    public static String toJson(Entity user, Gson gson, boolean stripSensitiveData) {
        if (user == null) {
            return gson.toJson(null);
        } else {
            user.setProperty("id", user.getKey().getId());
            if (stripSensitiveData) {
                user.removeProperty("password");
                // TODO: maybe remove other properties?
            }
            return gson.toJson(user.getProperties());
        }
    }

    public static String toJson(Iterable<Entity> users, Gson gson, boolean stripSensitiveData) {
        List<String> data = new ArrayList<String>();
        for (Entity e : users) {
            data.add(toJson(e, gson, stripSensitiveData));
        }
        return gson.toJson(data);
    }
}
