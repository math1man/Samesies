package com.dfaenterprises.samesies.model;

/**
 * @author Ari Weiland
 */
public class Question {

    private String q;
    private String a;
    private String category;

    public Question() {
    }

    public Question(String q) {
        this.q = q;
    }

    public Question(String q, String a) {
        this.q = q;
        this.a = a;
    }

    public Question(String q, String a, String category) {
        this.q = q;
        this.a = a;
        this.category = category;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
