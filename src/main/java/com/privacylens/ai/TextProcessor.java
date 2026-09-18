package com.privacylens.ai;

import com.privacylens.model.Chunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Text preprocessing: whitespace normalization, lowercasing, punctuation
 * handling, stopword removal, sentence splitting and chunk creation.
 *
 * Both the processed (tokenized/cleaned) form and the original readable
 * form are preserved, because the original text is what must be shown to
 * the user as evidence.
 */
@Component
public class TextProcessor {

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "if", "then", "so", "of", "to", "in",
            "on", "at", "by", "for", "with", "about", "against", "between", "into",
            "through", "during", "before", "after", "above", "below", "from", "up",
            "down", "is", "are", "was", "were", "be", "been", "being", "have", "has",
            "had", "having", "do", "does", "did", "doing", "will", "would", "shall",
            "should", "can", "could", "may", "might", "must", "this", "that", "these",
            "those", "i", "you", "he", "she", "it", "we", "they", "them", "their",
            "our", "your", "his", "her", "its", "as", "not", "no", "nor", "such",
            "than", "too", "very", "s", "t", "just", "also", "we'll", "us",
            // Question words are deliberately stopped: they carry no topical
            // meaning on their own and appear frequently throughout almost
            // any policy (e.g. "where" shows up in unrelated clauses like
            // "where you are requested to consent..."), which previously
            // caused irrelevant/random questions to falsely match evidence
            // just by sharing a WH-word with the policy text.
            "what", "why", "how", "when", "who", "where", "which", "whom", "whose"
    ));

    // Matches sentence-ending punctuation followed by whitespace + capital/quote,
    // or end of string. Handles common abbreviations reasonably well for
    // policy-style prose (not perfect NLP, but sufficient for this pipeline).
    private static final Pattern SENTENCE_SPLIT =
            Pattern.compile("(?<=[.!?])\\s+(?=[A-Z0-9\"'(])");

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern PUNCTUATION = Pattern.compile("[^a-z0-9\\s]");

    /**
     * Normalizes whitespace in raw extracted text (collapses runs of
     * spaces/tabs/newlines into single spaces, trims).
     */
    public String normalizeWhitespace(String text) {
        if (text == null) return "";
        return WHITESPACE.matcher(text.trim()).replaceAll(" ");
    }

    /**
     * Lowercases and strips punctuation, producing a "bag of words" style
     * string suitable for TF-IDF term extraction.
     */
    public String toProcessedForm(String text) {
        if (text == null) return "";
        String lower = text.toLowerCase();
        String noPunct = PUNCTUATION.matcher(lower).replaceAll(" ");
        return WHITESPACE.matcher(noPunct).replaceAll(" ").trim();
    }

    /**
     * Tokenizes processed text into terms with stopwords removed.
     */
    public List<String> tokenize(String processedText) {
        List<String> tokens = new ArrayList<>();
        if (processedText == null || processedText.isBlank()) return tokens;
        for (String word : processedText.split("\\s+")) {
            if (word.isBlank()) continue;
            if (STOPWORDS.contains(word)) continue;
            if (word.length() < 2) continue;
            tokens.add(word);
        }
        return tokens;
    }

    /**
     * Splits normalized text into sentences.
     */
    public List<String> splitSentences(String normalizedText) {
        List<String> sentences = new ArrayList<>();
        if (normalizedText == null || normalizedText.isBlank()) return sentences;
        for (String s : SENTENCE_SPLIT.split(normalizedText)) {
            String trimmed = s.trim();
            if (trimmed.length() >= 15) { // ignore fragments that are too short to be meaningful
                sentences.add(trimmed);
            }
        }
        return sentences;
    }

    /**
     * Builds chunks from raw policy text: normalizes, splits into sentences,
     * groups short sentences together (so each chunk carries enough context),
     * and produces both the original and processed forms for each chunk.
     */
    public List<Chunk> createChunks(String rawText) {
        List<Chunk> chunks = new ArrayList<>();
        String normalized = normalizeWhitespace(rawText);
        List<String> sentences = splitSentences(normalized);

        StringBuilder buffer = new StringBuilder();
        int index = 0;
        int wordCountInBuffer = 0;
        final int MIN_WORDS_PER_CHUNK = 12;

        for (String sentence : sentences) {
            if (buffer.length() > 0) buffer.append(" ");
            buffer.append(sentence);
            wordCountInBuffer += sentence.split("\\s+").length;

            if (wordCountInBuffer >= MIN_WORDS_PER_CHUNK) {
                String original = buffer.toString().trim();
                chunks.add(new Chunk(index++, original, toProcessedForm(original)));
                buffer.setLength(0);
                wordCountInBuffer = 0;
            }
        }
        // flush remaining tail as a final chunk
        if (buffer.length() > 0) {
            String original = buffer.toString().trim();
            chunks.add(new Chunk(index, original, toProcessedForm(original)));
        }
        return chunks;
    }
}
