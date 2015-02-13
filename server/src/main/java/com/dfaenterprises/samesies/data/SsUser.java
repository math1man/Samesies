package com.dfaenterprises.samesies.data;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;

/**
 * @author Ari Weiland
 */
public class SsUser {

    public static Entity makeUser(Key key, String email, String password, String location, String alias,
                                  String name, Integer age, String gender, String aboutMe, String[] questions) {
        Entity entity = makeUser(key, email, password, location, alias, name, age, gender, aboutMe);
        if (questions != null && questions.length > 0) {
            if (questions.length != 5) {
                throw new IllegalArgumentException("Exactly 0 or 5 questions must be specified!");
            }
            EmbeddedEntity questionsEntity = new EmbeddedEntity();
            for (int i=0; i<5; i++) {
                questionsEntity.setProperty("" + i, questions[i]);
            }
            entity.setUnindexedProperty("questions", questionsEntity);
        }
        return entity;
    }

    public static Entity makeUser(Key key, String email, String password, String location, String alias,
                                  String name, Integer age, String gender, String aboutMe) {
        Entity entity = makeUser(key, email, password, location, alias, name, age, gender);
        entity.setUnindexedProperty("aboutMe", aboutMe);
        return entity;
    }

    public static Entity makeUser(Key key, String email, String password, String location, String alias,
                                  String name, Integer age, String gender) {
        Entity entity = makeUser(key, email, password, location, alias);
        entity.setProperty("name", name);
        entity.setProperty("age", age);
        entity.setProperty("gender", gender);
        return entity;
    }

    public static Entity makeUser(Key key, String email, String password, String location, String alias) {
        Entity entity = makeUser(key, email, password, location);
        entity.setProperty("alias", alias);
        return entity;
    }

    public static Entity makeUser(Key key, String email, String password, String location) {
        Entity entity = new Entity(key);
        if (email == null || email.isEmpty()) {
            throw new NullPointerException("Must specify an email!");
        }
        if (password == null || password.isEmpty()) {
            throw new NullPointerException("Must specify a password!");
        }
        if (location == null || location.isEmpty()) {
            throw new NullPointerException("Must specify a location!");
        }
        User user = makeUserField(email);
        entity.setProperty("user", user);
        entity.setUnindexedProperty("password", password);
        entity.setProperty("location", location);
        entity.setProperty("alias", user.getNickname());
        EmbeddedEntity questionsEntity = new EmbeddedEntity();
        for (int i=0; i<5; i++) {
            questionsEntity.setProperty("" + i, "");
        }
        entity.setUnindexedProperty("questions", questionsEntity);
        return entity;
    }

    public static User makeUserField(String email) {
        return new User(email, "samesies.org");
    }
}
