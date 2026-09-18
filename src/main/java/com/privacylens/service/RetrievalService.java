package com.privacylens.service;

import com.privacylens.ai.RankingEngine;
import com.privacylens.ai.TFIDF;
import com.privacylens.model.Chunk;
import com.privacylens.model.SearchResult;
import com.privacylens.model.Topic;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RetrievalService {

    private static final int TOP_N_CANDIDATES = 8;

    private final TFIDF tfidf;
    private final RankingEngine rankingEngine;
    private final EvidenceValidator evidenceValidator;
    private final DocumentService documentService;

    public RetrievalService(
            TFIDF tfidf,
            RankingEngine rankingEngine,
            EvidenceValidator evidenceValidator,
            DocumentService documentService) {

        this.tfidf = tfidf;
        this.rankingEngine = rankingEngine;
        this.evidenceValidator = evidenceValidator;
        this.documentService = documentService;
    }

    public List<SearchResult> retrieveEvidence(
            String question) {

        List<Chunk> chunks =
                documentService.getChunks();

        Map<String, Double> corpusIdf =
                documentService.getCorpusIdf();

        if (chunks == null ||
                chunks.isEmpty() ||
                corpusIdf == null ||
                corpusIdf.isEmpty()) {

            return List.of();
        }

        Map<String, Double> questionVector =
                tfidf.vectorize(
                        question,
                        corpusIdf
                );

        List<SearchResult> ranked =
                rankingEngine.rank(
                        questionVector,
                        chunks,
                        TOP_N_CANDIDATES
                );

        Topic topic =
                evidenceValidator.classifyQuestion(
                        question
                );

        return evidenceValidator.validate(
                ranked,
                question,
                topic
        );
    }

    public List<SearchResult> retrieveEvidenceForTopic(
            String queryText,
            Topic topic) {

        List<Chunk> chunks =
                documentService.getChunks();

        Map<String, Double> corpusIdf =
                documentService.getCorpusIdf();

        if (chunks == null ||
                chunks.isEmpty() ||
                corpusIdf == null ||
                corpusIdf.isEmpty()) {

            return List.of();
        }

        Map<String, Double> queryVector =
                tfidf.vectorize(
                        queryText,
                        corpusIdf
                );

        List<SearchResult> ranked =
                rankingEngine.rank(
                        queryVector,
                        chunks,
                        TOP_N_CANDIDATES
                );

        return evidenceValidator.validate(
                ranked,
                topic
        );
    }
}