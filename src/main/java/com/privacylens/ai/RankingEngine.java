package com.privacylens.ai;

import com.privacylens.model.Chunk;
import com.privacylens.model.SearchResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Takes cosine similarity results across all policy chunks, sorts them by
 * relevance and returns the top matches as ranked SearchResult objects.
 *
 * Note: similarity scores are kept internal to the backend. They drive
 * ranking and the evidence-validation threshold, but are never surfaced
 * to the frontend as raw percentages.
 */
@Component
public class RankingEngine {

    private final CosineSimilarity cosineSimilarity;

    public RankingEngine(CosineSimilarity cosineSimilarity) {
        this.cosineSimilarity = cosineSimilarity;
    }

    /**
     * Scores every chunk against the question vector, sorts descending by
     * score and returns the top N results with rank assigned.
     */
    public List<SearchResult> rank(Map<String, Double> questionVector, List<Chunk> chunks, int topN) {
        List<SearchResult> results = new ArrayList<>();

        for (Chunk chunk : chunks) {
            double score = cosineSimilarity.compute(questionVector, chunk.getTfidfVector());
            results.add(new SearchResult(chunk.getOriginalText(), score));
        }

        results.sort(Comparator.comparingDouble(SearchResult::getScore).reversed());

        List<SearchResult> top = results.size() > topN ? results.subList(0, topN) : results;
        for (int i = 0; i < top.size(); i++) {
            top.get(i).setRank(i + 1);
        }
        return new ArrayList<>(top);
    }
}
