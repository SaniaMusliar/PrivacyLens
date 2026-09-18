package com.privacylens.service;

import com.privacylens.ai.TFIDF;
import com.privacylens.ai.TextProcessor;
import com.privacylens.model.Chunk;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Holds the currently loaded policy in memory. Only one active policy is
 * needed at a time (no database / multi-user persistence required for this
 * project's scope).
 */
@Service
public class DocumentService {

    private static final List<String> PRIVACY_SIGNALS = List.of(
            "privacy policy", "personal information", "personal data",
            "information we collect", "cookies", "third part", "data retention",
            "your rights", "privacy choices", "we collect", "share your information",
            "opt out", "opt-out", "gdpr", "ccpa"
    );

    private String documentName;
    private String documentType;
    private String rawText;
    private boolean policyLoaded = false;
    private boolean looksLikePrivacyPolicy = false;

    private List<Chunk> chunks = Collections.emptyList();
    private Map<String, Double> corpusIdf = Collections.emptyMap();

    private final TextProcessor textProcessor;
    private final TFIDF tfidf;

    public DocumentService(TextProcessor textProcessor, TFIDF tfidf) {
        this.textProcessor = textProcessor;
        this.tfidf = tfidf;
    }

    /**
     * Loads a new policy: stores metadata, builds chunks, and computes the
     * TF-IDF corpus vectors so retrieval is ready immediately.
     */
    public synchronized void setPolicy(String name, String type, String text) {
        this.documentName = name;
        this.documentType = type;
        this.rawText = text;
        this.policyLoaded = text != null && !text.isBlank();
        this.looksLikePrivacyPolicy = detectPrivacyPolicy(text);

        if (this.policyLoaded) {
            this.chunks = textProcessor.createChunks(text);
            this.corpusIdf = tfidf.buildCorpusVectors(this.chunks);
        } else {
            this.chunks = Collections.emptyList();
            this.corpusIdf = Collections.emptyMap();
        }
    }

    public synchronized String getPolicy() {
        return rawText;
    }

    public synchronized boolean hasPolicy() {
        return policyLoaded;
    }

    public synchronized void clearPolicy() {
        this.documentName = null;
        this.documentType = null;
        this.rawText = null;
        this.policyLoaded = false;
        this.looksLikePrivacyPolicy = false;
        this.chunks = Collections.emptyList();
        this.corpusIdf = Collections.emptyMap();
    }

    public synchronized String getDocumentName() {
        return documentName;
    }

    public synchronized String getDocumentType() {
        return documentType;
    }

    public synchronized boolean isLooksLikePrivacyPolicy() {
        return looksLikePrivacyPolicy;
    }

    public synchronized List<Chunk> getChunks() {
        return chunks;
    }

    public synchronized Map<String, Double> getCorpusIdf() {
        return corpusIdf;
    }

    public synchronized int getChunkCount() {
        return chunks.size();
    }

    /**
     * Lightweight structural signal check for whether the document appears
     * to actually be a privacy policy, per project requirement #10.
     * Deliberately conservative: requires multiple distinct signals before
     * concluding it's NOT a privacy policy, to avoid aggressive false
     * negatives on real (if unusually worded) policies.
     */
    private boolean detectPrivacyPolicy(String text) {
        if (text == null || text.isBlank()) return false;
        String lower = text.toLowerCase();
        int matches = 0;
        for (String signal : PRIVACY_SIGNALS) {
            if (lower.contains(signal)) {
                matches++;
            }
        }
        return matches >= 2;
    }
}
