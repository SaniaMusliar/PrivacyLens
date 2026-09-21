# PrivacyLens — Privacy Policy Analyzer

**PrivacyLens** is a Java Spring Boot application that helps users understand privacy policies through evidence-based question answering.

Users can upload or paste a privacy policy and ask questions about what it actually says. PrivacyLens retrieves relevant evidence from the policy, validates the evidence, and uses Retrieval-Augmented Generation (RAG) to produce a grounded response.

---

## Student Details

| Field             | Details                                         |
| ----------------- | ----------------------------------------------- |
| **Name**          | Sania Musliar                                   |
| **Programme**     | B.Tech Information Technology                   |
| **Department**    | Information Technology                          |
| **Institute**     | Fr. C. Rodrigues Institute of Technology, Vashi |
| **Academic Year** | 2026                                            |

## Problem Statement

Privacy policies are often lengthy and contain complex legal and technical terminology. Users may find it difficult to understand what personal information is collected, how it is used, whether it is shared, how long it is retained, and what controls are available.

**PrivacyLens** addresses this problem by allowing users to ask plain-language questions about a privacy policy and receive answers supported by evidence retrieved directly from the provided document.

## Objectives

* Simplify the understanding of lengthy privacy policies.
* Retrieve information relevant to a user's question.
* Provide answers grounded in the uploaded privacy policy.
* Display the evidence supporting each answer.
* Avoid unsupported conclusions and hallucinated information.
* Demonstrate an AI/NLP-based retrieval pipeline using Java.

## Key Features

* Upload privacy policies in **PDF, DOCX, or TXT** format.
* Paste privacy-policy text directly.
* Built-in sample policy for demonstration.
* Five-category privacy analysis:

  * Data Collection
  * Purpose
  * Data Usage / Sharing
  * Data Retention
  * User Controls
* Natural-language **Ask PrivacyLens** interface.
* Evidence-based question answering.
* Evidence displayed with every answer.
* Answer support status:

  * Directly Supported
  * Partially Supported
  * Not Clearly Stated
* Explicit handling of insufficient evidence.
* Lightweight privacy-policy detection.
* Java-based backend and AI/NLP processing.
* REST API architecture.
* RAG-based answer generation.

# Technology Stack

### Backend

* Java 17+
* Spring Boot
* Maven
* REST APIs

### AI / NLP

* Text Preprocessing
* TF-IDF
* Cosine Similarity
* Information Retrieval
* Evidence Ranking
* Evidence Validation
* Retrieval-Augmented Generation (RAG)

### Document Processing

* Apache PDFBox
* Apache POI
* Java File I/O

### LLM Integration

* OpenRouter API
* `openrouter/free` model router

### Frontend

* HTML
* CSS
* JavaScript

### Development Tools

* VS Code
* Git
* GitHub
* Maven

# System Architecture

                         PRIVACYLENS
                              │
                              ▼
                      Privacy Policy
                              │
                              ▼
                    Document Extraction
                  PDFBox / POI / Java I/O
                              │
                              ▼
                     Text Preprocessing
                              │
                              ▼
                    Sentence / Chunking
                              │
                              ▼
                           TF-IDF
                              │
                              ▼
                    Cosine Similarity
                              │
                              ▼
                     Evidence Ranking
                              │
                              ▼
                    Evidence Validation
                              │
                              ▼
                    Relevant Evidence
                              │
                              ▼
                      OpenRouter LLM
                              │
                              ▼
                       Grounded Answer
                              │
                              ▼
                  Answer + Evidence + Status


The complete backend pipeline is implemented using **Java and Spring Boot**.

The browser is responsible for displaying the interface and communicating with the Spring Boot REST APIs.


# Application Screenshots

## Home Dashboard

docs/screenshots/home.png

## Policy Upload

docs/screenshots/upload.png

## Policy Analysis

docs/screenshots/analysis.png

## Ask PrivacyLens

docs/screenshots/chatbot.png

# How PrivacyLens Works

### Step 1 — Upload or Paste Policy

The user uploads a PDF, DOCX, or TXT file, or directly pastes the privacy-policy text.

### Step 2 — Extract Text

The Java backend extracts readable text using PDFBox, Apache POI, or Java file processing.

### Step 3 — Preprocess and Chunk

The extracted policy is normalized and divided into smaller sentences or chunks.

### Step 4 — Convert Text into TF-IDF

The policy chunks and user question are converted into numerical TF-IDF representations.

### Step 5 — Calculate Similarity

Cosine similarity is used to compare the user's question with each policy chunk.

### Step 6 — Rank Evidence

The most relevant chunks are ranked and selected as candidate evidence.

### Step 7 — Validate Evidence

The system checks whether the retrieved evidence actually supports the user's specific question.

### Step 8 — Generate Answer

Validated evidence is supplied to the configured OpenRouter model as context.

### Step 9 — Display Result

The user receives:

* Answer
* Support status
* Evidence from the policy

# AI Concepts Implemented

| Concept               | Java File                                          | Purpose                                |
| --------------------- | -------------------------------------------------- | -------------------------------------- |
| Text Processing       | `TextProcessor.java`                               | Cleans and chunks policy text          |
| TF-IDF                | `TFIDF.java`                                       | Converts text into numerical vectors   |
| Cosine Similarity     | `CosineSimilarity.java`                            | Measures question-policy similarity    |
| Evidence Ranking      | `RankingEngine.java`                               | Ranks relevant policy chunks           |
| Information Retrieval | `RetrievalService.java`                            | Retrieves relevant policy evidence     |
| Evidence Validation   | `EvidenceValidator.java`                           | Checks evidence against the question   |
| RAG                   | `RetrievalService.java` + `OpenRouterService.java` | Provides retrieved evidence to the LLM |
| Answer Generation     | `ChatService.java`                                 | Produces the final grounded answer     |


# TF-IDF Implementation

For each policy chunk, TF-IDF is calculated using:

TF = term frequency / total terms in the chunk

IDF = log(N / document frequency)

TF-IDF = TF × IDF
```

The implementation is located in:

src/main/java/com/privacelens/ai/TFIDF.java

The user's question is represented using the same vocabulary and IDF values so that it can be compared with the policy chunks.

# Cosine Similarity

The similarity between the question vector and policy chunk vector is calculated using:

cosine similarity(A,B) =      A · B 
                        ------------------ 
                          (||A|| × ||B||)


Implementation:

src/main/java/com/privacelens/ai/CosineSimilarity.java

The resulting similarity values are used for ranking relevant policy chunks.

# Evidence Validation

Retrieval by similarity alone is not sufficient.

For example, if the user asks:

> **Does the company know my blood type?**

and the policy contains:

> **We may collect health information.**

The two concepts are related, but the evidence does not specifically establish that blood type is collected.

Therefore, `EvidenceValidator.java` performs an additional validation step.

It:

1. Classifies the question into a privacy-related topic.
2. Checks whether retrieved evidence is relevant.
3. Applies similarity thresholds.
4. Performs stricter matching for specific claims.
5. Rejects evidence that is too general to support a specific question.

This helps prevent unsupported conclusions.

# Retrieval-Augmented Generation

PrivacyLens uses a focused **Retrieval-Augmented Generation (RAG)** pipeline.

User Question
      │
      ▼
Question Processing
      │
      ▼
Retrieve Relevant Policy Chunks
      │
      ▼
Evidence Validation
      │
      ▼
Validated Evidence
      │
      ▼
OpenRouter LLM
      │
      ▼
Grounded Answer

The important principle is:

> **The privacy policy is the source of truth.**

The LLM is provided with retrieved policy evidence and instructed not to invent information or rely on unrelated external knowledge.

# Example 1 — Supported Question

### Question

Does Apple collect my location?

### Processing

Question
   ↓
TF-IDF
   ↓
Cosine Similarity
   ↓
Relevant Location Evidence
   ↓
Evidence Validation
   ↓
RAG
   ↓
Grounded Answer

### Result

The application displays the generated answer together with the relevant evidence retrieved from the policy.

# Example 2 — Insufficient Evidence

### Question

Does Apple know my blood type?

If the policy only contains a general statement such as:

We may collect health information.

PrivacyLens does not treat this as proof that blood type is collected.

### Result

STATUS

NOT CLEARLY STATED


The system responds:

I couldn't find sufficient information in the provided privacy policy
to answer this question.

This demonstrates the system's **uncertainty handling and evidence validation**.

# Structured Privacy Analysis

PrivacyLens also performs structured analysis across five categories:

                    Privacy Policy
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
 Data Collection       Purpose       Data Usage / Sharing
        │                                   │
        └─────────────────┬─────────────────┘
                          ▼
                   Data Retention
                          │
                          ▼
                    User Controls

Implementation:

src/main/java/com/privacelens/service/PrivacyAnalysisService.java

# Project Structure

PrivacyLens/
│
├── README.md
├── .gitignore
├── pom.xml
│
├── docs/
│   ├── architecture.png
│   └── screenshots/
│       ├── home.png
│       ├── upload.png
│       ├── analysis.png
│       └── chatbot.png
│
└── src/
    └── main/
        │
        ├── java/
        │   └── com/privacelens/
        │       │
        │       ├── PrivacyLensApplication.java
        │       │
        │       ├── ai/
        │       │   ├── TextProcessor.java
        │       │   ├── TFIDF.java
        │       │   ├── CosineSimilarity.java
        │       │   └── RankingEngine.java
        │       │
        │       ├── controller/
        │       │   ├── FileController.java
        │       │   ├── ChatController.java
        │       │   ├── AnalysisController.java
        │       │   └── GlobalExceptionHandler.java
        │       │
        │       ├── model/
        │       │
        │       └── service/
        │           ├── TextExtractionService.java
        │           ├── DocumentService.java
        │           ├── RetrievalService.java
        │           ├── ChatService.java
        │           ├── PrivacyAnalysisService.java
        │           ├── EvidenceValidator.java
        │           ├── OpenRouterService.java
        │           └── SamplePolicyProvider.java
        │
        └── resources/
            ├── application.properties
            └── static/
                ├── index.html
                ├── style.css
                └── app.js

# API Endpoints

| Method | Endpoint        | Description                     |
| ------ | --------------- | ------------------------------- |
| POST   | `/api/upload`   | Upload PDF / DOCX / TXT policy  |
| POST   | `/api/paste`    | Submit pasted policy text       |
| POST   | `/api/sample`   | Load sample policy              |
| POST   | `/api/clear`    | Clear current policy            |
| GET    | `/api/status`   | Get current policy status       |
| POST   | `/api/chat`     | Ask a privacy-policy question   |
| GET    | `/api/analysis` | Get structured privacy analysis |

# Demo Walkthrough

### 1. Start the application

```bash
mvn spring-boot:run
```
### 2. Open PrivacyLens

http://localhost:8080

### 3. Load a policy

Upload a PDF, DOCX, or TXT file, or use the sample policy.

### 4. Analyse the policy

Open the policy analysis section to view the five privacy categories.

### 5. Ask a question

Open **Ask PrivacyLens** and enter a question.

Example:

Does the company share my location?

### 6. Observe the retrieval pipeline

The system:

Question
   ↓
TF-IDF
   ↓
Cosine Similarity
   ↓
Evidence Ranking
   ↓
Evidence Validation
   ↓
RAG

### 7. View the result

The application displays:

* Generated answer
* Support status
* Evidence from the policy

### 8. Test uncertainty

Ask:

Does the company know my blood type?

The system should return:
NOT CLEARLY STATED
when sufficient evidence is not present.

# How to Run

## Requirements

* Java 17 or later
* Maven
* Internet connection for downloading Maven dependencies
* OpenRouter API key for RAG answer generation

## Clone

```bash
git clone https://github.com/SaniaMusliar/PrivacyLens.git
cd PrivacyLens
```

## Configure OpenRouter

The API key is **not stored in this repository**.

For Windows PowerShell:

```powershell
$env:OPENROUTER_API_KEY="YOUR_API_KEY"
```

The application reads it using:

```properties
openrouter.api.key=${OPENROUTER_API_KEY:}
```

## Build

```bash
mvn clean package -DskipTests
```

## Run

```bash
mvn spring-boot:run
```

Then open:

http://localhost:8080

# Security

The OpenRouter API key is not committed to GitHub.

The repository contains only:

openrouter.api.key=${OPENROUTER_API_KEY:}

The actual key is supplied through the environment variable.

The `.gitignore` also excludes:

* `.env` files
* Local secret configuration
* Maven build output
* IDE files
* Temporary files
* Logs

# CIA Scope

PrivacyLens implements a focused slice of a larger privacy-policy comprehension architecture.

### Implemented

Privacy Policy
      ↓
Text Preprocessing
      ↓
TF-IDF
      ↓
Cosine Similarity
      ↓
Evidence Ranking
      ↓
Evidence Validation
      ↓
RAG
      ↓
Evidence-Based Answer

The implementation demonstrates:

* Java programming
* Spring Boot
* Natural Language Processing
* Information Retrieval
* TF-IDF
* Cosine Similarity
* Evidence Validation
* Retrieval-Augmented Generation
* Evidence-grounded question answering

### Not Implemented

The current CIA implementation does not claim to implement:

* PP-BERT training
* Transformer fine-tuning
* Supervised PP-BERT classification
* Full research-paper architecture

These can be considered future extensions.

# Research Reference

The project was developed with reference to:

**Xin Zhang et al., "Enhanced Privacy Policy Comprehension via Pre-trained and Retrieval-Augmented Models," 2024 IEEE TrustCom.**

The research work provides the basis for applying retrieval-augmented techniques to privacy-policy comprehension.

PrivacyLens implements a smaller Java-based component focused on:

Policy
  ↓
Retrieval
  ↓
Evidence
  ↓
Validation
  ↓
RAG
  ↓
Answer

# Limitations

* Only one policy is stored in memory at a time.
* No database or multi-user persistence is implemented.
* Sentence splitting is regex-based.
* Scanned/image-only PDFs without a text layer cannot be processed because OCR is not included.
* Privacy-policy detection uses lightweight heuristic signals.
* Topic classification uses rule/keyword-based logic.
* Retrieval quality depends on the structure and wording of the uploaded policy.
* RAG generation depends on the availability and limits of the configured OpenRouter model.

# Future Scope

Possible extensions include:

* PP-BERT-based privacy-policy classification
* Transformer-based data-category and data-operation classification
* Dense semantic retrieval
* Maximum Marginal Relevance (MMR)
* OCR for scanned privacy policies
* Multi-document policy comparison
* Persistent policy storage
* Advanced privacy-rights detection
* Local open-source LLM deployment
* Advanced uncertainty estimation

# GitHub Repository

**Repository:**
https://github.com/SaniaMusliar/PrivacyLens

# Conclusion

PrivacyLens demonstrates a practical approach to privacy-policy comprehension using Java, information retrieval, evidence validation, and Retrieval-Augmented Generation.

Instead of relying only on an LLM, the system first retrieves relevant information from the user's privacy policy and validates whether that evidence actually supports the question.

This makes the response more transparent, allows users to inspect the supporting evidence, and helps reduce unsupported answers.
