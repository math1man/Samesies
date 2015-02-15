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
        if (questions != null && questions.length > 0) {
            if (questions.length != 5) {
                throw new IllegalArgumentException("Exactly 0 or 5 questions must be specified!");
            }
            EmbeddedEntity questionsEntity = new EmbeddedEntity();
            questionsEntity.setProperty("length", 5);
            for (int i=0; i<5; i++) {
                questionsEntity.setProperty("" + i, questions[i]);
            }
            entity.setUnindexedProperty("questions", questionsEntity);
        }
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
        EmbeddedEntity questionsEntity = new EmbeddedEntity();
        questionsEntity.setProperty("length", 5);
        for (int i=0; i<5; i++) {
            questionsEntity.setProperty("" + i, "");
        }
        entity.setUnindexedProperty("questions", questionsEntity);
        return entity;
    }

    public static String getAlias(String email) {
        return email.substring(0, email.indexOf('@'));
    }

    public static String toJson(Entity user, Gson gson, boolean stripSensitiveData) {
        if (user == null) {
            return gson.toJson(null);
        } else {
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
