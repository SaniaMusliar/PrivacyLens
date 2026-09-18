package com.privacylens.ai;

import com.privacylens.model.Chunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manual Java implementation of TF-IDF (Term Frequency - Inverse Document
 * Frequency). No external ML library is used.
 *
 * TF  = (number of times term appears in the chunk) / (total terms in chunk)
 * IDF = log(N / documentFrequency(term))
 * TF-IDF = TF * IDF
 */
@Component
public class TFIDF {

    private final TextProcessor textProcessor;

    public TFIDF(TextProcessor textProcessor) {
        this.textProcessor = textProcessor;
    }

    /**
     * Computes raw term frequency map for a list of tokens.
     */
    public Map<String, Double> termFrequency(List<String> tokens) {
        Map<String, Double> tf = new HashMap<>();
        if (tokens.isEmpty()) return tf;
        for (String token : tokens) {
            tf.merge(token, 1.0, Double::sum);
        }
        double total = tokens.size();
        for (Map.Entry<String, Double> entry : tf.entrySet()) {
            entry.setValue(entry.getValue() / total);
        }
        return tf;
    }

    /**
     * Computes inverse document frequency for every term across all chunks.
     * N = total number of chunks (documents).
     * documentFrequency(term) = number of chunks containing the term at least once.
     *
     * Uses log(N / df) with a +1 smoothing on df to avoid division by zero
     * for terms that only appear in the query and never in the corpus.
     */
    public Map<String, Double> inverseDocumentFrequency(List<List<String>> allChunkTokens) {
        Map<String, Double> idf = new HashMap<>();
        int n = allChunkTokens.size();
        if (n == 0) return idf;

        Map<String, Integer> docFrequency = new HashMap<>();
        for (List<String> tokens : allChunkTokens) {
            Set<String> uniqueTerms = new HashSet<>(tokens);
            for (String term : uniqueTerms) {
                docFrequency.merge(term, 1, Integer::sum);
            }
        }

        for (Map.Entry<String, Integer> entry : docFrequency.entrySet()) {
            double value = Math.log((double) n / entry.getValue());
            // Terms appearing in every chunk get idf 0 (log(1)); floor at a
            // small positive value so they aren't completely zeroed out.
            idf.put(entry.getKey(), Math.max(value, 0.01));
        }
        return idf;
    }

    /**
     * Builds TF-IDF vectors for every chunk and attaches them to the chunk
     * objects. Also returns the corpus-wide IDF map so the same IDF values
     * can be reused when vectorizing the user's question.
     */
    public Map<String, Double> buildCorpusVectors(List<Chunk> chunks) {
        List<List<String>> allTokens = new ArrayList<>();
        for (Chunk chunk : chunks) {
            allTokens.add(textProcessor.tokenize(chunk.getProcessedText()));
        }

        Map<String, Double> idf = inverseDocumentFrequency(allTokens);

        for (int i = 0; i < chunks.size(); i++) {
            Map<String, Double> tf = termFrequency(allTokens.get(i));
            Map<String, Double> tfidfVector = new HashMap<>();
            for (Map.Entry<String, Double> entry : tf.entrySet()) {
                double idfValue = idf.getOrDefault(entry.getKey(), 0.01);
                tfidfVector.put(entry.getKey(), entry.getValue() * idfValue);
            }
            chunks.get(i).setTfidfVector(tfidfVector);
        }
        return idf;
    }

    /**
     * Vectorizes an arbitrary piece of text (e.g. the user's question)
     * using an existing corpus IDF map, so it lives in the same vector
     * space as the policy chunks.
     */
    public Map<String, Double> vectorize(String rawText, Map<String, Double> corpusIdf) {
        String processed = textProcessor.toProcessedForm(rawText);
        List<String> tokens = textProcessor.tokenize(processed);
        Map<String, Double> tf = termFrequency(tokens);

        Map<String, Double> vector = new HashMap<>();
        for (Map.Entry<String, Double> entry : tf.entrySet()) {
            double idfValue = corpusIdf.getOrDefault(entry.getKey(), 0.5);
            vector.put(entry.getKey(), entry.getValue() * idfValue);
        }
        return vector;
    }
}
