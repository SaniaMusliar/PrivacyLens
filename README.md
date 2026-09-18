# PrivacyLens — Privacy Policy Analyzer

PrivacyLens is a Java Spring Boot application that lets you upload or paste a
privacy policy and get **evidence-based** answers about what it actually
says — no external LLM, no invented information, and no answers that go
beyond what the policy states.

This project was built for a B.Tech CIA to demonstrate a small, real,
information-retrieval pipeline implemented **entirely in Java**.

## Purpose

Privacy policies are long and hard to read. PrivacyLens lets a user ask
plain-language questions ("Is my information shared with third parties?")
and get back an answer that is directly grounded in a specific sentence (or
few sentences) from the policy — with that exact evidence shown alongside
the answer, so the user can verify it themselves.

## Features

- Upload a policy as **PDF, DOCX, or TXT**, or paste the text directly
- A built-in **sample policy** for one-click demonstration
- **Five-category structured analysis**: Data Collection, Purpose, Data
  Usage / Sharing, Data Retention, User Controls
- A **chat interface** ("Ask PrivacyLens") for free-form questions
- Every answer includes its **status** (Directly supported / Partially
  supported / Not clearly stated) and the **exact evidence sentence(s)**
  from the policy
- Clearly states when it **cannot find an answer** in the policy, rather
  than guessing or using outside knowledge
- Lightweight **privacy-policy detection** — warns if the uploaded document
  doesn't look like a privacy policy at all

## Architecture

```
Privacy Policy
      ↓
Text Extraction        (PDFBox / POI / Java file reading)
      ↓
Text Preprocessing     (normalize, lowercase, strip punctuation, stopwords)
      ↓
Sentence / Chunk Creation
      ↓
TF-IDF                 (manually implemented, no external ML library)
      ↓
Cosine Similarity      (manually implemented)
      ↓
Evidence Ranking       (top-N most similar chunks)
      ↓
Evidence Validation    (topic-aware relevance filter)
      ↓
Evidence-Based Answer
```

**PrivacyLens uses Java Spring Boot for the complete backend implementation,
including document processing, TF-IDF, cosine similarity, evidence
retrieval, evidence validation, and privacy analysis.** The browser only
renders the UI and calls the Spring Boot REST API — no analysis logic runs
in JavaScript.

### Package layout

```
com.privacylens
├── PrivacyLensApplication.java     Spring Boot entry point
├── ai/                             Core NLP/IR algorithms
│   ├── TextProcessor.java          Normalization, tokenizing, chunking
│   ├── TFIDF.java                  Manual TF-IDF implementation
│   ├── CosineSimilarity.java       Manual cosine similarity
│   └── RankingEngine.java          Sorts & ranks similarity results
├── controller/                     REST endpoints
│   ├── FileController.java         Upload / paste / sample / status
│   ├── ChatController.java         POST /api/chat
│   ├── AnalysisController.java     GET /api/analysis
│   └── GlobalExceptionHandler.java User-safe error responses
├── model/                          Plain data classes (DTOs)
└── service/                        Business logic
    ├── TextExtractionService.java  PDF/DOCX/TXT text extraction
    ├── DocumentService.java        In-memory current-policy state
    ├── RetrievalService.java       Orchestrates TF-IDF + ranking + validation
    ├── ChatService.java            Evidence-based Q&A
    ├── PrivacyAnalysisService.java Five-category structured analysis
    ├── EvidenceValidator.java      Topic classification & relevance filter
    └── SamplePolicyProvider.java   Built-in sample policy text
```

## How TF-IDF and cosine similarity work here

For every chunk of the policy:

- **TF** (term frequency) = how often a term appears in that chunk, divided
  by the chunk's total word count.
- **IDF** (inverse document frequency) = `log(N / documentFrequency(term))`,
  where `N` is the number of chunks and `documentFrequency` is how many
  chunks contain that term at least once.
- **TF-IDF** = `TF × IDF` for each term, forming a sparse vector per chunk.

The user's question is converted into a vector using the *same* IDF values
computed from the policy, so it lives in the same vector space. Each policy
chunk is then compared to the question vector with:

```
cosine similarity(A, B) = dot(A, B) / (‖A‖ × ‖B‖)
```

The chunks are sorted by similarity score and the top matches become
candidate evidence.

**Scores are internal.** They drive ranking and thresholds, but the raw
percentages are never shown in the UI — only qualitative labels
(*Directly supported* / *Partially supported* / *Not clearly stated*).

## Evidence validation

Retrieval by similarity alone isn't enough — a passage can share a word
with the question ("information") without actually being relevant (e.g. an
advertising-preferences sentence when the question is about account
signup data). `EvidenceValidator` classifies each question into a topic
(`DATA_COLLECTION`, `DATA_SHARING`, `RETENTION`, `USER_CONTROLS`,
`PURPOSE`, `ADVERTISING`, `GENERAL`) and checks that candidate evidence
contains topic-relevant keywords before it's accepted, on top of a minimum
similarity-score threshold.

## Insufficient evidence

If the policy doesn't contain enough relevant, validated evidence for a
question, PrivacyLens always returns:

> "I couldn't find sufficient information in the provided privacy policy to
> answer this question."

This is enforced even for questions where general world knowledge might
seem to "answer" the question (e.g. asking a company's revenue) — the
policy is the only source of truth PrivacyLens is allowed to use.

## API endpoints

| Method | Endpoint        | Description                                   |
|--------|-----------------|------------------------------------------------|
| POST   | `/api/upload`   | Upload a PDF / DOCX / TXT policy file           |
| POST   | `/api/paste`    | Submit pasted policy text (`{ "text": "..." }`) |
| POST   | `/api/sample`   | Load the built-in sample policy                 |
| POST   | `/api/clear`    | Clear the currently loaded policy               |
| GET    | `/api/status`   | Current policy load status                      |
| POST   | `/api/chat`     | Ask a question (`{ "question": "..." }`)        |
| GET    | `/api/analysis` | Five-category structured privacy analysis       |

## How to run

Requirements: **Java 17+** and **Maven** (with normal internet access to
Maven Central, so it can download Spring Boot, PDFBox and POI).

```bash
cd PrivacyLens
mvn spring-boot:run
```

Then open **http://localhost:8080** in your browser. No separate frontend
build step and no `npm install` are required — the Spring Boot app serves
the static frontend directly from `src/main/resources/static/`.

To run the test suite:

```bash
mvn test
```

## CIA scope

This project implements one focused slice of a larger conceptual
PrivacyLens architecture:

```
Privacy Policy → Preprocessing → TF-IDF → Cosine Similarity
              → Evidence Ranking → Evidence Validation → Evidence-Based Answer
```

This is classic **information retrieval** (IR), not deep learning. The
project does **not** implement or claim to implement PP-BERT, transformer
fine-tuning, supervised learning, or LLM-based generation — every answer is
assembled directly from retrieved and validated evidence using deterministic
Java logic.

## Limitations

- Single in-memory policy at a time (by design — no database/multi-tenant
  persistence needed for this scope).
- Sentence splitting is regex-based, not a full NLP sentence tokenizer, so
  unusual punctuation or list-heavy formatting may occasionally produce
  imperfect chunk boundaries.
- Scanned/image-only PDFs without a text layer cannot be extracted (no OCR).
- Privacy-policy detection is a lightweight heuristic (keyword/phrase
  signal count), not a trained classifier.
- Topic classification for evidence validation is rule/keyword based rather
  than a learned model — sufficient for this IR pipeline's scope, but not a
  general-purpose NLU system.
