package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

/**
 * @author Ari Weiland
 */
public class Feedback implements Storable {

    private Long id;
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

    public Feedback(Entity entity) {
        this.id = entity.getKey().getId();
        this.enjoy = (Long) entity.getProperty("enjoy");
        this.like = (String) entity.getProperty("like");
        this.dislike = (String) entity.getProperty("dislike");
        this.random = (String) entity.getProperty("random");
        this.randomComments = (String) entity.getProperty("randomComments");
        this.browse = (String) entity.getProperty("browse");
        this.browseComments = (String) entity.getProperty("browseComments");
        this.friends = (String) entity.getProperty("friends");
        this.friendsComments = (String) entity.getProperty("friendsComments");
        this.recommend = (String) entity.getProperty("recommend");
        this.buy = (String) entity.getProperty("buy");
        this.bored = (String) entity.getProperty("bored");
        this.interest = (String) entity.getProperty("interest");
        this.age = (Long) entity.getProperty("age");
        this.gender = (String) entity.getProperty("gender");
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
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
        Entity entity;
        if (id == null) {
            entity = new Entity("Feedback");
        } else {
            entity = new Entity("Feedback", id);
        }
        entity.setProperty("enjoy", enjoy);
        entity.setUnindexedProperty("like", like);
        entity.setUnindexedProperty("dislike", dislike);
        entity.setProperty("random", random);
        entity.setUnindexedProperty("randomComments", randomComments);
        entity.setProperty("browse", browse);
        entity.setUnindexedProperty("browseComments", browseComments);
        entity.setProperty("friends", friends);
        entity.setUnindexedProperty("friendsComments", friendsComments);
        entity.setProperty("recommend", recommend);
        entity.setProperty("buy", buy);
        entity.setProperty("bored", bored);
        entity.setUnindexedProperty("interest", interest);
        entity.setProperty("age", age);
        entity.setProperty("gender", gender);
        return entity;
    }
}
