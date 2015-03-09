package com.dfaenterprises.samesies.model;

import com.dfaenterprises.samesies.EntityUtils;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import org.mindrot.jbcrypt.BCrypt;

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
    private String newPassword;
    private String hashedPw;
    private String location;
    private String alias;
    private Text avatar;
    private String name;
    private Long age;
    private String gender;
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
        this.avatar = (Text) entity.getProperty("avatar");
        this.relation = relation;
        // private
        if (ordinal >= Relation.SELF.ordinal()) {
            this.email = (String) entity.getProperty("email");
            this.hashedPw = (String) entity.getProperty("hashedPw");
        }
        // protected
        if (ordinal >= Relation.FRIEND.ordinal()) {
            this.name = (String) entity.getProperty("name");
            this.age = (Long) entity.getProperty("age");
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
        this.avatar = getDefaultAvatar();
        this.name = name;
        this.age = Long.valueOf(age);
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

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getHashedPw() {
        return hashedPw;
    }

    public void setHashedPw(String hashedPw) {
        this.hashedPw = hashedPw;
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

    public String getAvatar() {
        return avatar.getValue();
    }

    public void setAvatar(String avatar) {
        if (avatar == null) {
            this.avatar = getDefaultAvatar();
        } else {
            this.avatar = new Text(avatar);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        if (age == null) {
            return null;
        } else {
            return age.intValue();
        }
    }

    public void setAge(Integer age) {
        this.age = Long.valueOf(age);
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
        if (password == null) {
            entity.setUnindexedProperty("hashedPw", hashedPw);
        } else {
            entity.setUnindexedProperty("hashedPw", BCrypt.hashpw(password, BCrypt.gensalt()));
        }
        entity.setProperty("location", location);
        entity.setProperty("alias", alias);
        entity.setProperty("avatar", avatar);
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

    private static Text getDefaultAvatar() {
        return new Text("img/lone_icon.png");
    }

    private static List<String> blankQuestions() {
        return Arrays.asList("", "", "", "", "");
    }
}
