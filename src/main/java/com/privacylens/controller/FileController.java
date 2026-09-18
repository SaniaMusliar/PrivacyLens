package com.privacylens.controller;

import com.privacylens.service.DocumentService;
import com.privacylens.service.SamplePolicyProvider;
import com.privacylens.service.TextExtractionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles document ingestion: file upload (PDF/DOCX/TXT), pasted text,
 * and loading the built-in sample policy.
 */
@RestController
@RequestMapping("/api")
public class FileController {

    private final TextExtractionService textExtractionService;
    private final DocumentService documentService;
    private final SamplePolicyProvider samplePolicyProvider;

    public FileController(TextExtractionService textExtractionService, DocumentService documentService,
                           SamplePolicyProvider samplePolicyProvider) {
        this.textExtractionService = textExtractionService;
        this.documentService = documentService;
        this.samplePolicyProvider = samplePolicyProvider;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return errorResponse(HttpStatus.BAD_REQUEST, "No file was uploaded.");
        }

        String filename = file.getOriginalFilename();
        String type = detectType(filename);

        try {
            String extractedText = textExtractionService.extract(file);
            documentService.setPolicy(filename, type, extractedText);
            return ResponseEntity.ok(buildLoadedResponse(extractedText));
        } catch (TextExtractionService.ExtractionException e) {
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while processing the file.");
        }
    }

    @PostMapping("/paste")
    public ResponseEntity<?> paste(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        if (text == null || text.isBlank()) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Pasted policy text cannot be empty.");
        }

        try {
            documentService.setPolicy("Pasted Policy", "TEXT", text);
            return ResponseEntity.ok(buildLoadedResponse(text));
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while processing the pasted text.");
        }
    }

    @PostMapping("/sample")
    public ResponseEntity<?> loadSample() {
        try {
            String sampleText = samplePolicyProvider.getSamplePolicyText();
            documentService.setPolicy("Sample Privacy Policy", "SAMPLE", sampleText);
            return ResponseEntity.ok(buildLoadedResponse(sampleText));
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while loading the sample policy.");
        }
    }

    @PostMapping("/clear")
    public ResponseEntity<?> clear() {
        documentService.clearPolicy();
        Map<String, Object> response = new HashMap<>();
        response.put("cleared", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("policyLoaded", documentService.hasPolicy());
        response.put("documentName", documentService.getDocumentName());
        response.put("documentType", documentService.getDocumentType());
        response.put("looksLikePrivacyPolicy", documentService.isLooksLikePrivacyPolicy());
        response.put("chunkCount", documentService.getChunkCount());
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> buildLoadedResponse(String extractedText) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("documentName", documentService.getDocumentName());
        response.put("documentType", documentService.getDocumentType());
        response.put("looksLikePrivacyPolicy", documentService.isLooksLikePrivacyPolicy());
        response.put("chunkCount", documentService.getChunkCount());

        String preview = extractedText.length() > 600 ? extractedText.substring(0, 600) + "..." : extractedText;
        response.put("preview", preview.trim());

        if (!documentService.isLooksLikePrivacyPolicy()) {
            response.put("warning",
                    "This document does not appear to be a privacy policy. Privacy analysis may not be meaningful.");
        }
        return response;
    }

    private String detectType(String filename) {
        if (filename == null) return "UNKNOWN";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "PDF";
        if (lower.endsWith(".docx")) return "DOCX";
        if (lower.endsWith(".txt")) return "TXT";
        return "UNKNOWN";
    }

    private ResponseEntity<?> errorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }
}
