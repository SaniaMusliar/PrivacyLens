package com.privacylens.controller;

import com.privacylens.model.ChatRequest;
import com.privacylens.model.ChatResponse;
import com.privacylens.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles POST /api/chat - evidence-based question answering over the
 * currently loaded policy.
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        try {
            ChatResponse response = chatService.answer(request == null ? null : request.getQuestion());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> body = new HashMap<>();
            body.put("error", "An unexpected error occurred while answering your question.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }
}
