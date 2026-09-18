package com.privacylens.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Extracts plain text from uploaded PDF, DOCX and TXT files using
 * Apache PDFBox, Apache POI and standard Java file reading respectively.
 */
@Service
public class TextExtractionService {

    /**
     * Thrown when text cannot be extracted from a document. Carries a
     * user-safe message (no stack traces are ever shown to the user).
     */
    public static class ExtractionException extends RuntimeException {
        public ExtractionException(String message) {
            super(message);
        }

        public ExtractionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public String extract(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new ExtractionException("The uploaded file has no name.");
        }

        String lower = filename.toLowerCase();
        try {
            if (lower.endsWith(".pdf")) {
                return extractPdf(file.getInputStream());
            } else if (lower.endsWith(".docx")) {
                return extractDocx(file.getInputStream());
            } else if (lower.endsWith(".txt")) {
                return extractTxt(file.getInputStream());
            } else {
                throw new ExtractionException(
                        "Unsupported file type. Please upload a PDF, DOCX, or TXT file.");
            }
        } catch (ExtractionException e) {
            throw e;
        } catch (IOException e) {
            throw new ExtractionException("Could not read the uploaded file.", e);
        }
    }

    private String extractPdf(InputStream inputStream) {
        try (PDDocument document = PDDocument.load(inputStream)) {
            if (document.isEncrypted()) {
                throw new ExtractionException("The PDF is password-protected and cannot be read.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null || text.isBlank()) {
                throw new ExtractionException(
                        "Could not extract any text from this PDF. It may be a scanned image without a text layer.");
            }
            return text;
        } catch (ExtractionException e) {
            throw e;
        } catch (Exception e) {
            throw new ExtractionException("Failed to extract text from the PDF file.", e);
        }
    }

    private String extractDocx(InputStream inputStream) {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            if (text == null || text.isBlank()) {
                throw new ExtractionException("Could not extract any text from this DOCX file.");
            }
            return text;
        } catch (ExtractionException e) {
            throw e;
        } catch (Exception e) {
            throw new ExtractionException("Failed to extract text from the DOCX file.", e);
        }
    }

    private String extractTxt(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            String text = new String(bytes, StandardCharsets.UTF_8);
            if (text.isBlank()) {
                throw new ExtractionException("The uploaded TXT file is empty.");
            }
            return text;
        } catch (ExtractionException e) {
            throw e;
        } catch (IOException e) {
            throw new ExtractionException("Failed to read the TXT file.", e);
        }
    }
}
