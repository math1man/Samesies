package com.dfaenterprises.samesies.data;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ari Weiland
 */
public class SsUser {

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
