package com.dfaenterprises.samesies.model;

/**
 * @author Ari Weiland
 */
public class Episode {
    private Question[] questions;

    public Episode() {
    }

    public Episode(Question[] questions) {
        this.questions = questions;
    }

    public Question[] getQuestions() {
        return questions;
    }

    public void setQuestions(Question[] questions) {
        this.questions = questions;
    }
}
