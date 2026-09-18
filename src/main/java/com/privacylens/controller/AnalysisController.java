package com.privacylens.controller;

import com.privacylens.model.AnalysisResult;
import com.privacylens.service.PrivacyAnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles GET /api/analysis - the structured five-category privacy
 * analysis of the currently loaded policy.
 */
@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final PrivacyAnalysisService privacyAnalysisService;

    public AnalysisController(PrivacyAnalysisService privacyAnalysisService) {
        this.privacyAnalysisService = privacyAnalysisService;
    }

    @GetMapping("/analysis")
    public ResponseEntity<?> analyze() {
        try {
            AnalysisResult result = privacyAnalysisService.analyze();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> body = new HashMap<>();
            body.put("error", "An unexpected error occurred while analyzing the policy.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }
}
