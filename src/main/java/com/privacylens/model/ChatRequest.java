package com.privacylens.model;

/**
 * Request body for POST /api/chat
 */
public class ChatRequest {

    private String question;

    public ChatRequest() {
    }

    public ChatRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
