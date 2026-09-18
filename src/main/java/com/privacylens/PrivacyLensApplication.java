package com.privacylens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PrivacyLens - Evidence-based privacy policy analyzer.
 *
 * Entry point for the Spring Boot application. Serves the static frontend
 * from src/main/resources/static and exposes REST APIs for document upload,
 * privacy analysis and evidence-based question answering.
 *
 * Run with: mvn spring-boot:run
 * Then open: http://localhost:8080
 */
@SpringBootApplication
public class PrivacyLensApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrivacyLensApplication.class, args);
    }
}
