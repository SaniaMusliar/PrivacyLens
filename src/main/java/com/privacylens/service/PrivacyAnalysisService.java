package com.privacylens.service;

import com.privacylens.model.AnalysisResult;
import com.privacylens.model.ChatResponse;
import com.privacylens.model.PrivacyFinding;
import com.privacylens.model.SearchResult;
import com.privacylens.model.Topic;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Produces the structured five-category privacy analysis:
 *
 * 1. Data Collection
 * 2. Purpose
 * 3. Data Usage / Sharing
 * 4. Data Retention
 * 5. User Controls
 *
 * These are broad analytical categories, so broad topic validation
 * is appropriate here.
 */
@Service
public class PrivacyAnalysisService {

    private static final int MAX_EVIDENCE_ITEMS = 3;

    private static final Map<String, Topic> CATEGORY_TOPICS =
            new LinkedHashMap<>();

    private static final Map<String, String> CATEGORY_QUERIES =
            new LinkedHashMap<>();

    static {

        CATEGORY_TOPICS.put(
                "Data Collection",
                Topic.DATA_COLLECTION
        );

        CATEGORY_TOPICS.put(
                "Purpose",
                Topic.PURPOSE
        );

        CATEGORY_TOPICS.put(
                "Data Usage / Sharing",
                Topic.DATA_SHARING
        );

        CATEGORY_TOPICS.put(
                "Data Retention",
                Topic.RETENTION
        );

        CATEGORY_TOPICS.put(
                "User Controls",
                Topic.USER_CONTROLS
        );

        CATEGORY_QUERIES.put(
                "Data Collection",
                "What personal information and data does the company collect from users, such as name, email, address, device information, location, identifiers, or payment information?"
        );

        CATEGORY_QUERIES.put(
                "Purpose",
                "Why is personal information collected and for what purpose is it used?"
        );

        CATEGORY_QUERIES.put(
                "Data Usage / Sharing",
                "Does the company share, sell, transfer, or disclose personal information with third parties, service providers, partners, or affiliates?"
        );

        CATEGORY_QUERIES.put(
                "Data Retention",
                "How long is personal information retained, stored, or kept by the company?"
        );

        CATEGORY_QUERIES.put(
                "User Controls",
                "Can users access, delete, correct, update, manage, or opt out of the use of their personal information?"
        );
    }

    private final RetrievalService retrievalService;
    private final DocumentService documentService;

    public PrivacyAnalysisService(
            RetrievalService retrievalService,
            DocumentService documentService) {

        this.retrievalService = retrievalService;
        this.documentService = documentService;
    }

    public AnalysisResult analyze() {

        AnalysisResult result =
                new AnalysisResult();

        result.setPolicyLoaded(
                documentService.hasPolicy());

        result.setDocumentName(
                documentService.getDocumentName());

        result.setLooksLikePrivacyPolicy(
                documentService.isLooksLikePrivacyPolicy());

        // ==========================================
        // NO POLICY
        // ==========================================

        if (!documentService.hasPolicy()) {

            result.setMessage(
                    "No policy is loaded yet. Please upload or paste a privacy policy first."
            );

            result.setFindings(
                    List.of()
            );

            return result;
        }

        // ==========================================
        // NON-PRIVACY DOCUMENT
        // ==========================================

        if (!documentService.isLooksLikePrivacyPolicy()) {

            result.setMessage(
                    "This document does not appear to be a privacy policy. Privacy analysis may not be meaningful."
            );
        }

        // ==========================================
        // FIVE CATEGORY ANALYSIS
        // ==========================================

        List<PrivacyFinding> findings =
                new ArrayList<>();

        for (Map.Entry<String, Topic> entry
                : CATEGORY_TOPICS.entrySet()) {

            String category =
                    entry.getKey();

            Topic topic =
                    entry.getValue();

            String query =
                    CATEGORY_QUERIES.get(category);

            findings.add(
                    buildFinding(
                            category,
                            topic,
                            query)
            );
        }

        result.setFindings(findings);

        return result;
    }

    private PrivacyFinding buildFinding(
            String category,
            Topic topic,
            String query) {

        /*
         * Broad category retrieval.
         *
         * This intentionally does NOT require every word in the
         * query to occur in the evidence.
         */
        List<SearchResult> evidence =
                retrievalService.retrieveEvidenceForTopic(
                        query,
                        topic
                );

        // ==========================================
        // NO EVIDENCE
        // ==========================================

        if (evidence.isEmpty()) {

            return new PrivacyFinding(
                    category,
                    "NOT_CLEARLY_STATED",
                    "The policy does not clearly state this.",
                    List.of()
            );
        }

        /*
         * IMPORTANT:
         *
         * Do NOT use raw similarity score to decide
         * DIRECTLY_SUPPORTED vs PARTIALLY_SUPPORTED.
         *
         * The existence of validated topic evidence means
         * the category is supported.
         *
         * For this broad five-category analysis, classify
         * validated evidence as directly supported.
         */
        String status =
                "DIRECTLY_SUPPORTED";

        String explanation =
                buildExplanation(
                        category);

        // ==========================================
        // EVIDENCE
        // ==========================================

        List<ChatResponse.Evidence> evidenceList =
                new ArrayList<>();

        int limit =
                Math.min(
                        MAX_EVIDENCE_ITEMS,
                        evidence.size());

        for (int i = 0; i < limit; i++) {

            evidenceList.add(
                    new ChatResponse.Evidence(
                            evidence.get(i).getOriginalText()
                    )
            );
        }

        return new PrivacyFinding(
                category,
                status,
                explanation,
                evidenceList
        );
    }

    private String buildExplanation(
            String category) {

        return switch (category) {

            case "Data Collection" ->
                    "The policy contains evidence describing information collected from users.";

            case "Purpose" ->
                    "The policy contains evidence describing why information is collected or how it is used.";

            case "Data Usage / Sharing" ->
                    "The policy contains evidence describing how information may be used or shared.";

            case "Data Retention" ->
                    "The policy contains evidence describing how information is retained, stored, or kept.";

            case "User Controls" ->
                    "The policy contains evidence describing options available to users for managing their information.";

            default ->
                    "The policy contains evidence related to this category.";
        };
    }
}