package com.dfaenterprises.samesies.model;

import com.dfaenterprises.samesies.EntityUtils;
import com.google.appengine.api.datastore.Entity;

import java.util.Date;

/**
 * @author Ari Weiland
 */
public class Message extends Storable {

    private Long chatId;
    private Long senderId;
    private String message;
    private Date sentDate;
    private String random;

    public Message() {
    }

    public Message(Entity e) {
        super(e);
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
        this.random = (random == null ? EntityUtils.randomString(20) : random);
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
        Entity e = getEntity("Message");
        e.setProperty("chatId", chatId);
        e.setProperty("senderId", senderId);
        e.setUnindexedProperty("message", message);
        e.setProperty("sentDate", sentDate);
        e.setUnindexedProperty("random", random);
        return e;
    }
}
