package com.privacylens.ai;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Manual Java implementation of cosine similarity between two sparse
 * TF-IDF vectors, represented as term -> weight maps.
 *
 * cosine similarity = dot(A, B) / (||A|| * ||B||)
 */
@Component
public class CosineSimilarity {

    public double compute(Map<String, Double> vectorA, Map<String, Double> vectorB) {
        if (vectorA == null || vectorB == null || vectorA.isEmpty() || vectorB.isEmpty()) {
            return 0.0;
        }

        double dotProduct = dotProduct(vectorA, vectorB);
        double magnitudeA = magnitude(vectorA);
        double magnitudeB = magnitude(vectorB);

        if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            return 0.0;
        }

        return dotProduct / (magnitudeA * magnitudeB);
    }

    private double dotProduct(Map<String, Double> a, Map<String, Double> b) {
        // Iterate over the smaller map for efficiency.
        Map<String, Double> smaller = a.size() <= b.size() ? a : b;
        Map<String, Double> larger = a.size() <= b.size() ? b : a;

        Set<String> terms = new HashSet<>(smaller.keySet());
        double sum = 0.0;
        for (String term : terms) {
            Double valB = larger.get(term);
            if (valB != null) {
                sum += smaller.get(term) * valB;
            }
        }
        return sum;
    }

    private double magnitude(Map<String, Double> vector) {
        double sumSquares = 0.0;
        for (double value : vector.values()) {
            sumSquares += value * value;
        }
        return Math.sqrt(sumSquares);
    }
}
