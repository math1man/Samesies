package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class User {

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

    public User() {

    }

    public User(Entity entity) {
        this.id = entity.getKey().getId();
        this.email = (String) entity.getProperty("email");
        this.password = (String) entity.getProperty("password");
        this.location = (String) entity.getProperty("location");
        this.alias = (String) entity.getProperty("alias");
        if (entity.hasProperty("name")) {
            this.name = (String) entity.getProperty("name");
        }
        if (entity.hasProperty("age")) {
            this.age = ((Long) entity.getProperty("age")).intValue();
        }
        if (entity.hasProperty("gender")) {
            this.gender = (String) entity.getProperty("gender");
        }
        if (entity.hasProperty("aboutMe")) {
            this.aboutMe = (String) entity.getProperty("aboutMe");
        }
        if (entity.hasProperty("questions")) {
            EmbeddedEntity qs = (EmbeddedEntity) entity.getProperty("questions");
            this.questions = new String[5];
            for (int i=0; i<5; i++) {
                this.questions[i] = (String) qs.getProperty("" + i);
            }
        } else {
            this.questions = emptyQuestions();
        }
    }

    public User(String email, String password, String location) {
        this(email, password, location, null);
    }

    public User(String email, String password, String location, String alias) {
        this.email = email;
        this.password = password;
        this.location = location;
        if (alias == null) {
            this.alias = getAlias(email);
        } else {
            this.alias = alias;
        }
        this.questions = emptyQuestions();
    }

    public User(String email, String password, String location, String alias,
                String name, Integer age, String gender) {
        this.email = email;
        this.password = password;
        this.location = location;
        this.alias = alias;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.questions = emptyQuestions();
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
        this.questions = emptyQuestions();
    }

    public User(String email, String password, String location, String alias,
                String name, Integer age, String gender, String aboutMe, String[] questions) {
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

    public User(Long id, String email, String password, String location, String alias,
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
        if (name != null) {
            entity.setProperty("name", name);
        }
        if (age != null) {
            entity.setProperty("age", age);
        }
        if (gender != null) {
            entity.setProperty("gender", gender);
        }
        if (aboutMe != null) {
            entity.setUnindexedProperty("aboutMe", aboutMe);
        }
        entity.setUnindexedProperty("questions", makeQuestions(questions));
        return entity;
    }

    public void removeSensitiveData() {
        this.password = null;
        // TODO: remove other stuff?
    }

    public User fromEntity(Entity entity) {
        return new User(entity);
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

    private static String[] emptyQuestions() {
        return new String[]{"", "", "", "", ""};
    }
}
