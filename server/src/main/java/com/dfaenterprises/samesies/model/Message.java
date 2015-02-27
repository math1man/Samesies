package com.dfaenterprises.samesies.model;

import com.google.appengine.api.datastore.Entity;

import java.util.Date;

/**
 * @author Ari Weiland
 */
public class Message implements Storable {

    private Long id;
    private Long chatId;
    private Long senderId;
    private String message;
    private Date sentDate;
    private String random;

    public Message() {
    }

    public Message(Entity e) {
        this.id = e.getKey().getId();
        this.chatId = (Long) e.getProperty("chatId");
        this.senderId = (Long) e.getProperty("senderId");
        this.message = (String) e.getProperty("message");
        this.sentDate = (Date) e.getProperty("sentDate");
        this.random = (String) e.getProperty("random");
    }

    public Message(Long chatId, Long senderId, String message) {
        this(chatId, senderId, message, null);
    }

    public Message(Long chatId, Long senderId, String message, String random) {
        this.chatId = chatId;
        this.senderId = senderId;
        this.message = message;
        this.sentDate = new Date();
        this.random = (random == null ? randomId() : random);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public String getRandom() {
        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }

    @Override
    public Entity toEntity() {
        Entity entity;
        if (id == null) {
            entity = new Entity("Message");
        } else {
            entity = new Entity("Message", id);
        }
        entity.setProperty("chatId", chatId);
        entity.setProperty("senderId", senderId);
        entity.setUnindexedProperty("message", message);
        entity.setProperty("sentDate", sentDate);
        entity.setUnindexedProperty("random", random);
        return entity;
    }

    public static String randomId() {
        StringBuilder sb = new StringBuilder();
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i=0; i<20; i++) {
            sb.append(possible.charAt((int) (Math.random() * possible.length())));
        }
        return sb.toString();
    }
}
