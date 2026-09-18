package com.privacylens.service;

import com.privacylens.model.SearchResult;
import com.privacylens.model.Topic;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EvidenceValidator {

    private static final double MIN_SCORE_THRESHOLD = 0.06;
    private static final double STRONG_SCORE_THRESHOLD = 0.18;

    private final Map<Topic, List<String>> topicKeywords = new EnumMap<>(Topic.class);

    public EvidenceValidator() {

        topicKeywords.put(
                Topic.DATA_COLLECTION,
                Arrays.asList(
                        "collect",
                        "collection",
                        "information we collect",
                        "personal information",
                        "personal data",
                        "data we collect",
                        "gather",
                        "obtain"
                )
        );

        topicKeywords.put(
                Topic.DATA_SHARING,
                Arrays.asList(
                        "share",
                        "sharing",
                        "third party",
                        "third parties",
                        "disclose",
                        "disclosure",
                        "service provider",
                        "partners"
                )
        );

        topicKeywords.put(
                Topic.RETENTION,
                Arrays.asList(
                        "retain",
                        "retention",
                        "stored",
                        "storage",
                        "keep",
                        "delete",
                        "deletion",
                        "period"
                )
        );

        topicKeywords.put(
                Topic.USER_CONTROLS,
                Arrays.asList(
                        "access",
                        "delete",
                        "remove",
                        "opt out",
                        "control",
                        "choice",
                        "preferences",
                        "settings"
                )
        );

        topicKeywords.put(
                Topic.PURPOSE,
                Arrays.asList(
                        "purpose",
                        "use",
                        "used",
                        "provide",
                        "improve",
                        "services",
                        "operate"
                )
        );

        topicKeywords.put(
                Topic.ADVERTISING,
                Arrays.asList(
                        "advertising",
                        "advertisement",
                        "ads",
                        "marketing",
                        "advertisers",
                        "targeted"
                )
        );
    }

    /**
     * Minimum relevance score required before evidence is considered.
     */
    public double getMinScoreThreshold() {
        return MIN_SCORE_THRESHOLD;
    }

    /**
     * Strong retrieval score.
     *
     * IMPORTANT:
     * This score must NOT by itself determine whether a specific
     * privacy claim is directly supported.
     */
    public double getStrongScoreThreshold() {
        return STRONG_SCORE_THRESHOLD;
    }

    /**
     * Classifies a user's question into a privacy-policy topic.
     */
    public Topic classifyQuestion(String question) {

        if (question == null || question.isBlank()) {
            return Topic.DATA_COLLECTION;
        }

        String q = question.toLowerCase(Locale.ROOT);

        if (containsAny(q,
                "retain",
                "retention",
                "how long",
                "stored",
                "storage",
                "keep my data",
                "delete my data")) {

            return Topic.RETENTION;
        }

        if (containsAny(q,
                "share",
                "shared",
                "third party",
                "third parties",
                "disclose",
                "sell my data")) {

            return Topic.DATA_SHARING;
        }

        if (containsAny(q,
                "delete",
                "remove",
                "access my data",
                "opt out",
                "control",
                "change my information")) {

            return Topic.USER_CONTROLS;
        }

        if (containsAny(q,
                "why",
                "purpose",
                "used for",
                "use my data")) {

            return Topic.PURPOSE;
        }

        if (containsAny(q,
                "advertising",
                "advertisement",
                "ads",
                "marketing",
                "advertisers")) {

            return Topic.ADVERTISING;
        }

        return Topic.DATA_COLLECTION;
    }

    /**
     * Broad validation used for policy-analysis sections.
     */
    public List<SearchResult> validate(
            List<SearchResult> results,
            Topic topic) {

        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }

        return results.stream()
                .filter(Objects::nonNull)
                .filter(result -> result.getOriginalText() != null)
                .filter(result -> !result.getOriginalText().isBlank())
                .filter(result -> result.getScore() >= MIN_SCORE_THRESHOLD)
                .filter(result -> matchesTopic(
                        result.getOriginalText(),
                        topic))
                .sorted(Comparator.comparingDouble(
                        SearchResult::getScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Strict validation for chatbot questions.
     *
     * Retrieval only finds candidate evidence.
     * This method determines whether the evidence actually supports
     * the specific claim asked by the user.
     */
    public List<SearchResult> validate(
            List<SearchResult> results,
            String question,
            Topic topic) {

        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }

        List<SearchResult> validated = new ArrayList<>();

        for (SearchResult result : results) {

            if (result == null) {
                continue;
            }

            String evidence = result.getOriginalText();

            if (evidence == null || evidence.isBlank()) {
                continue;
            }

            if (result.getScore() < MIN_SCORE_THRESHOLD) {
                continue;
            }

            /*
             * First use retrieval relevance.
             */
            if (!matchesTopic(evidence, topic)) {
                continue;
            }

            /*
             * Then perform semantic/lexical claim validation.
             *
             * Example:
             *
             * Question:
             * "Does Apple collect my blood type?"
             *
             * Evidence:
             * "Health Information"
             *
             * This MUST NOT be treated as direct evidence for
             * "blood type".
             */
            if (!isExactClaimSupported(question, evidence, topic)) {
                continue;
            }

            validated.add(result);
        }

        return validated.stream()
                .sorted(Comparator.comparingDouble(
                        SearchResult::getScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Determines whether a specific user claim is actually supported
     * by the retrieved policy sentence.
     */
    public boolean isExactClaimSupported(
            String question,
            String evidence,
            Topic topic) {

        if (question == null ||
                question.isBlank() ||
                evidence == null ||
                evidence.isBlank()) {

            return false;
        }

        String q = normalize(question);
        String e = normalize(evidence);

        /*
         * Broad questions do not require an exact attribute match.
         */
        if (!requiresSpecificClaimMatch(q)) {
            return matchesTopic(evidence, topic);
        }

        /*
         * Extract the specific thing the user is asking about.
         */
        Set<String> specificTerms = extractSpecificTerms(q);

        if (specificTerms.isEmpty()) {
            return false;
        }

        /*
         * The evidence must explicitly mention the requested
         * attribute or an accepted equivalent.
         */
        for (String term : specificTerms) {

            if (hasEquivalentTerm(term, e)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Specific questions require attribute-level evidence.
     */
    private boolean requiresSpecificClaimMatch(String question) {

        if (question == null || question.isBlank()) {
            return false;
        }

        String q = normalize(question);

        return q.contains("does")
                || q.contains("do ")
                || q.contains("is ")
                || q.contains("are ")
                || q.contains("can ")
                || q.contains("will ")
                || q.contains("what ")
                || q.contains("which ")
                || q.contains("whether")
                || q.contains("collect my")
                || q.contains("collect your")
                || q.contains("store my")
                || q.contains("share my")
                || q.contains("use my");
    }

    /**
     * Extracts meaningful attribute terms from a question.
     */
    private Set<String> extractSpecificTerms(String question) {

        String q = normalize(question);

        Set<String> terms = new LinkedHashSet<>();

        String[] words = q.split("\\s+");

        Set<String> ignored = new HashSet<>(Arrays.asList(
                "does",
                "do",
                "did",
                "is",
                "are",
                "was",
                "were",
                "can",
                "could",
                "will",
                "would",
                "may",
                "might",
                "the",
                "a",
                "an",
                "my",
                "your",
                "their",
                "our",
                "me",
                "you",
                "they",
                "we",
                "collect",
                "collection",
                "use",
                "used",
                "using",
                "store",
                "stored",
                "share",
                "shared",
                "keep",
                "retain",
                "retained",
                "information",
                "data",
                "about",
                "from",
                "with",
                "for",
                "to",
                "of",
                "in",
                "on",
                "privacy",
                "policy"
        ));

        for (String word : words) {

            word = word.replaceAll("[^a-z0-9-]", "");

            if (word.length() < 3) {
                continue;
            }

            if (ignored.contains(word)) {
                continue;
            }

            terms.add(word);
        }

        addCompoundTerms(q, terms);

        return terms;
    }

    /**
     * Adds important multi-word privacy attributes.
     */
    private void addCompoundTerms(
            String question,
            Set<String> terms) {

        if (question.contains("blood type")) {
            terms.add("blood type");
        }

        if (question.contains("phone number")) {
            terms.add("phone number");
        }

        if (question.contains("telephone number")) {
            terms.add("telephone number");
        }

        if (question.contains("mobile number")) {
            terms.add("mobile number");
        }

        if (question.contains("email address")) {
            terms.add("email address");
        }

        if (question.contains("home address")) {
            terms.add("home address");
        }

        if (question.contains("postal address")) {
            terms.add("postal address");
        }

        if (question.contains("precise location")) {
            terms.add("precise location");
        }

        if (question.contains("geolocation")) {
            terms.add("geolocation");
        }

        if (question.contains("date of birth")) {
            terms.add("date of birth");
        }

        if (question.contains("birth date")) {
            terms.add("birth date");
        }

        if (question.contains("credit card")) {
            terms.add("credit card");
        }

        if (question.contains("bank account")) {
            terms.add("bank account");
        }

        if (question.contains("financial information")) {
            terms.add("financial information");
        }

        if (question.contains("health information")) {
            terms.add("health information");
        }
    }

    /**
     * Checks accepted equivalents for specific attributes.
     *
     * IMPORTANT:
     * Generic categories such as "health information" do NOT
     * automatically prove "blood type".
     */
    private boolean hasEquivalentTerm(
            String term,
            String evidence) {

        if (term == null || evidence == null) {
            return false;
        }

        String t = normalize(term);
        String e = normalize(evidence);

        /*
         * Blood type requires explicit blood-type evidence.
         *
         * "Health information" alone is NOT enough.
         */
        if (t.equals("blood type")
                || t.equals("blood")) {

            return e.contains("blood type")
                    || e.contains("blood group")
                    || e.contains("abo blood")
                    || e.contains("rhesus")
                    || e.contains("rh factor");
        }

        /*
         * Phone number requires phone/telephone/mobile evidence.
         *
         * "Contact information" alone is NOT enough.
         */
        if (t.equals("phone")
                || t.equals("number")
                || t.equals("phone number")
                || t.equals("telephone number")
                || t.equals("mobile number")) {

            return e.contains("phone number")
                    || e.contains("telephone number")
                    || e.contains("mobile number")
                    || e.contains("telephone")
                    || e.contains("phone")
                    || e.contains("mobile number");
        }

        /*
         * Email.
         */
        if (t.equals("email")
                || t.equals("email address")) {

            return e.contains("email")
                    || e.contains("email address");
        }

        /*
         * Location.
         */
        if (t.equals("location")
                || t.equals("precise location")
                || t.equals("geolocation")) {

            return e.contains("location")
                    || e.contains("geolocation")
                    || e.contains("gps")
                    || e.contains("precise location")
                    || e.contains("coarse location");
        }

        /*
         * Home / postal address.
         */
        if (t.equals("address")
                || t.equals("home address")
                || t.equals("postal address")) {

            return e.contains("address")
                    || e.contains("postal address")
                    || e.contains("home address");
        }

        /*
         * Date of birth.
         */
        if (t.equals("date of birth")
                || t.equals("birth date")
                || t.equals("birth")) {

            return e.contains("date of birth")
                    || e.contains("birth date")
                    || e.contains("birthday");
        }

        /*
         * Credit card.
         */
        if (t.equals("credit card")
                || t.equals("card")) {

            return e.contains("credit card")
                    || e.contains("debit card")
                    || e.contains("payment card");
        }

        /*
         * Bank account.
         */
        if (t.equals("bank account")
                || t.equals("bank")) {

            return e.contains("bank account")
                    || e.contains("banking information")
                    || e.contains("bank details");
        }

        /*
         * For ordinary specific terms, require the actual
         * word/phrase to occur in the evidence.
         */
        return containsWholeWordOrPhrase(e, t);
    }

    /**
     * Checks whether a piece of evidence is relevant to the
     * requested broad privacy topic.
     */
    private boolean matchesTopic(
            String evidence,
            Topic topic) {

        if (evidence == null || evidence.isBlank()) {
            return false;
        }

        if (topic == null) {
            return true;
        }

        String text = evidence.toLowerCase(Locale.ROOT);

        List<String> keywords =
                topicKeywords.getOrDefault(
                        topic,
                        Collections.emptyList());

        for (String keyword : keywords) {

            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        return false;
    }

    private boolean containsAny(
            String text,
            String... terms) {

        for (String term : terms) {

            if (text.contains(term)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsWholeWordOrPhrase(
            String text,
            String term) {

        if (text.contains(term)) {
            return true;
        }

        String pattern =
                "\\b" +
                java.util.regex.Pattern.quote(term) +
                "\\b";

        return java.util.regex.Pattern
                .compile(pattern)
                .matcher(text)
                .find();
    }

    private String normalize(String text) {

        return text
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}