package com.dfaenterprises.samesies.model;

import com.dfaenterprises.samesies.EntityUtils;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Text;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ari Weiland
 */
public class User extends Storable {

    public static enum Status {
        PENDING, ACTIVATED, DELETED
    }

    public static enum Relation {
        STRANGER, FRIEND, SELF, ADMIN
    }

    private String email;
    private String password;
    private String newPassword;
    private String hashedPw;
    private String alias;
    private Text avatar;
    private Float latitude;
    private Float longitude;
    private String name;
    private Long age;
    private String gender;
    private String aboutMe;
    private List<String> questions;

    private Status status;
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
        this.alias = (String) e.getProperty("alias");
        this.avatar = (Text) e.getProperty("z_avatar");
        setLocation((GeoPt) e.getProperty("location"));
        this.gender = (String) e.getProperty("gender");
        this.aboutMe = (String) e.getProperty("aboutMe");
        this.questions = EntityUtils.getListProp(e, "questions", 5, String.class);
        this.status = EntityUtils.getEnumProp(e, "status", Status.class);
        this.isBanned = (Boolean) e.getProperty("isBanned");
        // protected
        if (ordinal >= Relation.FRIEND.ordinal()) {
            this.name = (String) e.getProperty("name");
            this.age = (Long) e.getProperty("age");
        }
        // private
        if (ordinal >= Relation.SELF.ordinal()) {
            this.email = (String) e.getProperty("email");
            this.hashedPw = (String) e.getProperty("hashedPw");
        }
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

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    public GeoPt getLocation() {
        if (hasLocation()) {
            return new GeoPt(latitude, longitude);
        } else {
            return null;
        }
    }

    public void setLocation(GeoPt location) {
        if (location != null) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
        }
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Boolean getIsBanned() {
        return isBanned;
    }

    public void setIsBanned(Boolean isBanned) {
        this.isBanned = isBanned;
    }

    public String getDisplayName() {
        if (name == null) {
            return alias;
        } else {
            return name;
        }
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
        this.status = Status.PENDING;
        this.isBanned = false;
    }

    public Entity toEntity() {
        Entity e = getEntity("User");
        e.setProperty("email", email);
        if (password != null) {
            hashedPw = BCrypt.hashpw(password, BCrypt.gensalt());
        }
        e.setUnindexedProperty("hashedPw", hashedPw);
        e.setProperty("alias", alias);
        e.setUnindexedProperty("z_avatar", avatar);
        e.setUnindexedProperty("location", getLocation());
        e.setProperty("name", name);
        e.setProperty("age", age);
        e.setProperty("gender", gender);
        e.setUnindexedProperty("aboutMe", aboutMe);
        EntityUtils.setListProp(e, "questions", questions);
        e.setProperty("status", status.name());
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
