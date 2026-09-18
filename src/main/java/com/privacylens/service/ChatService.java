package com.privacylens.service;

import com.privacylens.model.ChatResponse;
import com.privacylens.model.SearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private static final String INSUFFICIENT_EVIDENCE =
            "I couldn't find sufficient information in the provided privacy policy to answer this question.";

    private static final int MAX_EVIDENCE = 3;

    private final RetrievalService retrievalService;
    private final DocumentService documentService;
    private final OpenRouterService openRouterService;

    public ChatService(
            RetrievalService retrievalService,
            DocumentService documentService,
            OpenRouterService openRouterService) {

        this.retrievalService = retrievalService;
        this.documentService = documentService;
        this.openRouterService = openRouterService;
    }

    public ChatResponse answer(String question) {

        if (question == null ||
                question.isBlank()) {

            return new ChatResponse(
                    "Please enter a question about the policy.",
                    "NOT_CLEARLY_STATED",
                    List.of()
            );
        }

        if (!documentService.hasPolicy()) {

            return new ChatResponse(
                    "No policy is loaded yet. Please upload or paste a privacy policy first.",
                    "NOT_CLEARLY_STATED",
                    List.of()
            );
        }

        /*
         * RAG — RETRIEVAL
         */
        List<SearchResult> evidence =
                retrievalService.retrieveEvidence(
                        question
                );

        /*
         * No validated evidence:
         * do not let the LLM guess.
         */
        if (evidence.isEmpty()) {

            return new ChatResponse(
                    INSUFFICIENT_EVIDENCE,
                    "NOT_CLEARLY_STATED",
                    List.of()
            );
        }

        List<String> evidenceText =
                new ArrayList<>();

        int limit =
                Math.min(
                        MAX_EVIDENCE,
                        evidence.size()
                );

        for (int i = 0; i < limit; i++) {

            evidenceText.add(
                    evidence.get(i)
                            .getOriginalText()
            );
        }

        /*
         * RAG — GENERATION
         */
        try {

            String answer =
                    openRouterService.generateAnswer(
                            question,
                            evidenceText
                    );

            return new ChatResponse(
                    answer,
                    "DIRECTLY_SUPPORTED",
                    buildEvidence(evidence)
            );

        } catch (
                OpenRouterService.OpenRouterException e) {

            /*
             * Retrieval still succeeded even if
             * the generation provider fails.
             */
            return new ChatResponse(
                    "Relevant evidence was found, but the answer could not be generated. "
                            + "Please check the OpenRouter configuration.",
                    "PARTIALLY_SUPPORTED",
                    buildEvidence(evidence)
            );
        }
    }

    /*
     * Compatibility with the existing ChatController
     * if it currently calls chat.ask(...).
     */
    public ChatResponse ask(String question) {
        return answer(question);
    }

    private List<ChatResponse.Evidence> buildEvidence(
            List<SearchResult> evidence) {

        List<ChatResponse.Evidence> result =
                new ArrayList<>();

        int limit =
                Math.min(
                        MAX_EVIDENCE,
                        evidence.size()
                );

        for (int i = 0; i < limit; i++) {

            result.add(
                    new ChatResponse.Evidence(
                            evidence.get(i)
                                    .getOriginalText()
                    )
            );
        }

        return result;
    }
}