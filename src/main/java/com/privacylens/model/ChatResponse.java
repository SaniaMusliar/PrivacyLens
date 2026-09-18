package com.privacylens.model;

import java.util.List;

/**
 * Response body for POST /api/chat
 */
public class ChatResponse {

    private String answer;
    private String status;
    private List<Evidence> evidence;

    public ChatResponse() {
    }

    public ChatResponse(String answer, String status, List<Evidence> evidence) {
        this.answer = answer;
        this.status = status;
        this.evidence = evidence;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Evidence> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<Evidence> evidence) {
        this.evidence = evidence;
    }

    /**
     * A single piece of supporting evidence shown to the user.
     * Intentionally exposes only the original policy text - never the raw
     * similarity score, which is an internal ranking signal only.
     */
    public static class Evidence {
        private String text;

        public Evidence() {
        }

        public Evidence(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
