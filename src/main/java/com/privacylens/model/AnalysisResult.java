package com.privacylens.model;

import java.util.List;

/**
 * Response body for GET /api/analysis - the full five-category
 * privacy analysis of the currently loaded policy.
 */
public class AnalysisResult {

    private boolean policyLoaded;
    private boolean looksLikePrivacyPolicy;
    private String documentName;
    private List<PrivacyFinding> findings;
    private String message;

    public AnalysisResult() {
    }

    public boolean isPolicyLoaded() {
        return policyLoaded;
    }

    public void setPolicyLoaded(boolean policyLoaded) {
        this.policyLoaded = policyLoaded;
    }

    public boolean isLooksLikePrivacyPolicy() {
        return looksLikePrivacyPolicy;
    }

    public void setLooksLikePrivacyPolicy(boolean looksLikePrivacyPolicy) {
        this.looksLikePrivacyPolicy = looksLikePrivacyPolicy;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public List<PrivacyFinding> getFindings() {
        return findings;
    }

    public void setFindings(List<PrivacyFinding> findings) {
        this.findings = findings;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
