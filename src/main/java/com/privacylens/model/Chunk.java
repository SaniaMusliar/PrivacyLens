package com.privacylens.model;

import java.util.Map;

/**
 * A single unit of the policy text (roughly a sentence or short group of
 * sentences) used as the atomic unit for TF-IDF and retrieval.
 *
 * Both the original, human-readable text and the processed (normalized,
 * stopword-stripped) tokens are kept: the processed tokens feed the maths,
 * the original text is what gets shown to the user as evidence.
 */
public class Chunk {

    private final int index;
    private final String originalText;
    private final String processedText;
    private Map<String, Double> tfidfVector;

    public Chunk(int index, String originalText, String processedText) {
        this.index = index;
        this.originalText = originalText;
        this.processedText = processedText;
    }

    public int getIndex() {
        return index;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getProcessedText() {
        return processedText;
    }

    public Map<String, Double> getTfidfVector() {
        return tfidfVector;
    }

    public void setTfidfVector(Map<String, Double> tfidfVector) {
        this.tfidfVector = tfidfVector;
    }
}
