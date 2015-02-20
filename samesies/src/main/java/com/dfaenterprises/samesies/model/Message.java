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

    public Message() {
    }

    public Message(Entity e) {
        this.id = e.getKey().getId();
        this.chatId = (Long) e.getProperty("chatId");
        this.senderId = (Long) e.getProperty("senderId");
        this.message = (String) e.getProperty("message");
        this.sentDate = (Date) e.getProperty("sentDate");
    }

    public Message(Long chatId, Long senderId, String message) {
        this.chatId = chatId;
        this.senderId = senderId;
        this.message = message;
        this.sentDate = new Date();
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
        return entity;
    }
}
