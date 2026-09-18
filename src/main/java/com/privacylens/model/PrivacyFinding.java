package com.privacylens.model;

import java.util.List;

/**
 * One category's finding within the overall privacy analysis
 * (e.g. Data Collection, Purpose, Data Usage / Sharing, Data Retention,
 * User Controls).
 */
public class PrivacyFinding {

    private String category;
    private String status;
    private String explanation;
    private List<ChatResponse.Evidence> evidence;

    public PrivacyFinding() {
    }

    public PrivacyFinding(String category, String status, String explanation,
                           List<ChatResponse.Evidence> evidence) {
        this.category = category;
        this.status = status;
        this.explanation = explanation;
        this.evidence = evidence;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<ChatResponse.Evidence> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<ChatResponse.Evidence> evidence) {
        this.evidence = evidence;
    }
}
