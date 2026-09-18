package com.privacylens.model;

/**
 * Internal representation of a ranked chunk of evidence.
 *
 * The similarity score is deliberately kept internal-only: RankingEngine
 * and EvidenceValidator use it to sort and filter, but no controller ever
 * serializes it back to the client. Raw percentages like "30.6%" must
 * never reach the UI (see project requirement #6).
 */
public class SearchResult {

    private final String originalText;
    private final double score;
    private int rank;

    public SearchResult(String originalText, double score) {
        this.originalText = originalText;
        this.score = score;
    }

    public String getOriginalText() {
        return originalText;
    }

    public double getScore() {
        return score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
