package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class Feedback extends Storable {

    private Long enjoy;
    private String like;
    private String dislike;
    private String random;
    private String randomComments;
    private String browse;
    private String browseComments;
    private String friends;
    private String friendsComments;
    private String recommend;
    private String buy;
    private String bored;
    private String interest;
    private Long age;
    private String gender;

    public Feedback() {
    }

    public Feedback(Entity e) {
        super(e);
        this.enjoy = (Long) e.getProperty("enjoy");
        this.like = (String) e.getProperty("like");
        this.dislike = (String) e.getProperty("dislike");
        this.random = (String) e.getProperty("random");
        this.randomComments = (String) e.getProperty("randomComments");
        this.browse = (String) e.getProperty("browse");
        this.browseComments = (String) e.getProperty("browseComments");
        this.friends = (String) e.getProperty("friends");
        this.friendsComments = (String) e.getProperty("friendsComments");
        this.recommend = (String) e.getProperty("recommend");
        this.buy = (String) e.getProperty("buy");
        this.bored = (String) e.getProperty("bored");
        this.interest = (String) e.getProperty("interest");
        this.age = (Long) e.getProperty("age");
        this.gender = (String) e.getProperty("gender");
    }

    public Long getEnjoy() {
        return enjoy;
    }

    public void setEnjoy(Long enjoy) {
        this.enjoy = enjoy;
    }

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public String getDislike() {
        return dislike;
    }

    public void setDislike(String dislike) {
        this.dislike = dislike;
    }

    public String getRandom() {
        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }

    public String getRandomComments() {
        return randomComments;
    }

    public void setRandomComments(String randomComments) {
        this.randomComments = randomComments;
    }

    public String getBrowse() {
        return browse;
    }

    public void setBrowse(String browse) {
        this.browse = browse;
    }

    public String getBrowseComments() {
        return browseComments;
    }

    public void setBrowseComments(String browseComments) {
        this.browseComments = browseComments;
    }

    public String getFriends() {
        return friends;
    }

    public void setFriends(String friends) {
        this.friends = friends;
    }

    public String getFriendsComments() {
        return friendsComments;
    }

    public void setFriendsComments(String friendsComments) {
        this.friendsComments = friendsComments;
    }

    public String getRecommend() {
        return recommend;
    }

    public void setRecommend(String recommend) {
        this.recommend = recommend;
    }

    public String getBuy() {
        return buy;
    }

    public void setBuy(String buy) {
        this.buy = buy;
    }

    public String getBored() {
        return bored;
    }

    public void setBored(String bored) {
        this.bored = bored;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public Entity toEntity() {
        Entity e = getEntity("Feedback");
        e.setProperty("enjoy", enjoy);
        e.setUnindexedProperty("like", like);
        e.setUnindexedProperty("dislike", dislike);
        e.setProperty("random", random);
        e.setUnindexedProperty("randomComments", randomComments);
        e.setProperty("browse", browse);
        e.setUnindexedProperty("browseComments", browseComments);
        e.setProperty("friends", friends);
        e.setUnindexedProperty("friendsComments", friendsComments);
        e.setProperty("recommend", recommend);
        e.setProperty("buy", buy);
        e.setProperty("bored", bored);
        e.setUnindexedProperty("interest", interest);
        e.setProperty("age", age);
        e.setProperty("gender", gender);
        return e;
    }
}
