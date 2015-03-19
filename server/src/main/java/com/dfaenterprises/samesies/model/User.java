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
public class User extends Storable {

    public static enum Relation {
        STRANGER, FRIEND, SELF, ADMIN
    }

    private String email;
    private String password;
    private String newPassword;
    private String hashedPw;
    private String community;
    // **Low-Priority** TODO: remove location eventually, needed for compatibility
    private String location;
    private String alias;
    private Text avatar;
    private String name;
    private Long age;
    private String gender;
    private String aboutMe;
    private List<String> questions;

    private Boolean isActivated;
    private Boolean isBanned;

    public User() {
    }

    public User(Entity entity) {
        this(entity, Relation.ADMIN);
    }

    public User(Entity e, Relation relation) {
        super(e);
        int ordinal = relation.ordinal();

        // public
        this.community = (String) e.getProperty("community");
        this.location = community;
        this.alias = (String) e.getProperty("alias");
        this.avatar = (Text) e.getProperty("avatar");
        this.isBanned = (Boolean) e.getProperty("isBanned");

        // private
        if (ordinal >= Relation.SELF.ordinal()) {
            this.email = (String) e.getProperty("email");
            this.hashedPw = (String) e.getProperty("hashedPw");
            this.isActivated = (Boolean) e.getProperty("isActivated");
        }
        // protected
        if (ordinal >= Relation.FRIEND.ordinal()) {
            this.name = (String) e.getProperty("name");
            this.age = (Long) e.getProperty("age");
            this.gender = (String) e.getProperty("gender");
            this.aboutMe = (String) e.getProperty("aboutMe");
            this.questions = EntityUtils.entityToList(e.getProperty("questions"), 5, String.class);
        }
    }

    public User(String email, String password, String community, String alias,
                String name, Integer age, String gender, String aboutMe) {
        this.email = email;
        this.password = password;
        this.community = community;
        this.location = community;
        this.alias = alias;
        this.avatar = getDefaultAvatar();
        this.name = name;
        this.age = Long.valueOf(age);
        this.gender = gender;
        this.aboutMe = aboutMe;
        this.questions = blankQuestions();
        this.isActivated = true;
        this.isBanned = false;
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

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
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
        if (avatar == null) {
            setDefaultAvatar();
        }
        return avatar.getValue();
    }

    public void setAvatar(String avatar) {
        if (avatar == null) {
            setDefaultAvatar();
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

    public Boolean getIsActivated() {
        return isActivated;
    }

    public void setIsActivated(Boolean isActivated) {
        this.isActivated = isActivated;
    }

    public Boolean getIsBanned() {
        return isBanned;
    }

    public void setIsBanned(Boolean isBanned) {
        this.isBanned = isBanned;
    }

    public void setDefaultAlias() {
        if (email != null) {
            setAlias(getAlias(email));
        }
    }

    public void setDefaultAvatar() {
        this.avatar = getDefaultAvatar();
    }

    public void setBlankQuestions() {
        questions = blankQuestions();
    }

    public void initNewUser() {
        if (alias == null) {
            setDefaultAlias();
        }
        if (avatar == null) {
            setDefaultAvatar();
        }
        setBlankQuestions();
        this.isActivated = false;
        this.isBanned = false;
    }

    public Entity toEntity() {
        Entity e = getEntity("User");
        e.setProperty("email", email);
        if (password != null) {
            hashedPw = BCrypt.hashpw(password, BCrypt.gensalt());
        }
        e.setUnindexedProperty("hashedPw", hashedPw);
        String community = this.community;
        if (community == null) {
            community = this.location;
        }
        e.setProperty("community", community);
        e.setProperty("alias", alias);
        e.setUnindexedProperty("avatar", avatar);
        e.setProperty("name", name);
        e.setProperty("age", age);
        e.setProperty("gender", gender);
        e.setUnindexedProperty("aboutMe", aboutMe);
        e.setUnindexedProperty("questions", EntityUtils.listToEntity(questions));
        e.setProperty("isActivated", isActivated);
        e.setProperty("isBanned", isBanned);
        return e;
    }

    public static String getAlias(String email) {
        return email.substring(0, email.indexOf('@'));
    }

    public static Text getDefaultAvatar() {
        return new Text("img/lone_icon.png");
    }

    public static List<String> blankQuestions() {
        return Arrays.asList("", "", "", "", "");
    }
}
