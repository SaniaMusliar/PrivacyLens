// PrivacyLens frontend
// All analysis logic lives in the Java Spring Boot backend. This script
// only handles UI state and talks to the REST API at /api/*.

const API_BASE = "/api";

const state = {
  policyLoaded: false,
  documentName: null,
  documentType: null,
  looksLikePrivacyPolicy: true,
};

// ---------------- Navigation ----------------

const views = ["dashboard", "upload", "analysis", "chat"];

function showView(viewName) {
  views.forEach((v) => {
    document.getElementById(`view-${v}`).classList.toggle("hidden", v !== viewName);
  });
  document.querySelectorAll(".nav-item").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.view === viewName);
  });

  if (viewName === "analysis" && state.policyLoaded) {
    loadAnalysis(false);
  }
}

document.querySelectorAll(".nav-item").forEach((btn) => {
  btn.addEventListener("click", () => showView(btn.dataset.view));
});

// ---------------- Helpers ----------------

async function apiPost(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    method: "POST",
    ...options,
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    const message = data && data.error ? data.error : "Something went wrong. Please try again.";
    throw new Error(message);
  }
  return data;
}

async function apiGet(path) {
  const res = await fetch(`${API_BASE}${path}`);
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    const message = data && data.error ? data.error : "Something went wrong. Please try again.";
    throw new Error(message);
  }
  return data;
}

function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str == null ? "" : str;
  return div.innerHTML;
}

function statusLabel(status) {
  switch (status) {
    case "DIRECTLY_SUPPORTED": return "Directly supported";
    case "PARTIALLY_SUPPORTED": return "Partially supported";
    case "NOT_CLEARLY_STATED": return "Not clearly stated";
    default: return status || "Unknown";
  }
}

function statusClass(status) {
  switch (status) {
    case "DIRECTLY_SUPPORTED": return "status-supported";
    case "PARTIALLY_SUPPORTED": return "status-partial";
    default: return "status-none";
  }
}

// ---------------- Policy loaded state sync ----------------

function applyLoadedResponse(data) {
  state.policyLoaded = true;
  state.documentName = data.documentName;
  state.documentType = data.documentType;
  state.looksLikePrivacyPolicy = data.looksLikePrivacyPolicy;

  const sidebarStatus = document.getElementById("sidebarPolicyStatus");
  sidebarStatus.innerHTML = `<span class="dot ${data.looksLikePrivacyPolicy ? "dot-loaded" : "dot-warn"}"></span><span>${escapeHtml(data.documentName)}</span>`;

  document.getElementById("dashboardHero").classList.add("hidden");
  document.getElementById("dashboardLoaded").classList.remove("hidden");

  document.getElementById("loadedMeta").textContent =
    `${data.documentName} · ${data.documentType} · ${data.chunkCount} sections`;

  const warningEl = document.getElementById("nonPolicyWarning");
  if (!data.looksLikePrivacyPolicy) {
    warningEl.textContent = "This document does not appear to be a privacy policy. Privacy analysis may not be meaningful.";
    warningEl.classList.remove("hidden");
  } else {
    warningEl.classList.add("hidden");
  }

  document.getElementById("policyPreview").textContent = data.preview || "";

  loadAnalysis(true);
}

function resetLoadedState() {
  state.policyLoaded = false;
  document.getElementById("dashboardHero").classList.remove("hidden");
  document.getElementById("dashboardLoaded").classList.add("hidden");
  document.getElementById("sidebarPolicyStatus").innerHTML =
    `<span class="dot dot-empty"></span><span>No policy loaded</span>`;
  document.getElementById("chatWindow").innerHTML = "";
  document.getElementById("analysisContent").innerHTML =
    `<div class="empty-state">Load a policy from the Dashboard or Upload page to see analysis.</div>`;
}

// ---------------- Dashboard buttons ----------------

document.getElementById("heroUploadBtn").addEventListener("click", () => showView("upload"));
document.getElementById("heroPasteBtn").addEventListener("click", () => {
  showView("upload");
  document.getElementById("pasteTextarea").focus();
});
document.getElementById("heroSampleBtn").addEventListener("click", loadSample);
document.getElementById("dashboardClearBtn").addEventListener("click", clearPolicy);
document.getElementById("viewFullAnalysisBtn").addEventListener("click", () => showView("analysis"));
document.getElementById("askFromDashboardBtn").addEventListener("click", () => showView("chat"));

async function loadSample() {
  try {
    const data = await apiPost("/sample");
    applyLoadedResponse(data);
    showView("dashboard");
  } catch (err) {
    alert(err.message);
  }
}

async function clearPolicy() {
  try {
    await apiPost("/clear");
    resetLoadedState();
    showView("dashboard");
  } catch (err) {
    alert(err.message);
  }
}

// ---------------- Upload page ----------------

const dropzone = document.getElementById("dropzone");
const fileInput = document.getElementById("fileInput");

dropzone.addEventListener("click", () => fileInput.click());

dropzone.addEventListener("dragover", (e) => {
  e.preventDefault();
  dropzone.classList.add("dragover");
});
dropzone.addEventListener("dragleave", () => dropzone.classList.remove("dragover"));
dropzone.addEventListener("drop", (e) => {
  e.preventDefault();
  dropzone.classList.remove("dragover");
  if (e.dataTransfer.files.length > 0) {
    handleFileUpload(e.dataTransfer.files[0]);
  }
});

fileInput.addEventListener("change", () => {
  if (fileInput.files.length > 0) {
    handleFileUpload(fileInput.files[0]);
  }
});

async function handleFileUpload(file) {
  hideUploadError();
  const formData = new FormData();
  formData.append("file", file);

  try {
    const data = await apiPost("/upload", { body: formData });
    showUploadResult(data);
    applyLoadedResponse(data);
    showView("analysis");
  } catch (err) {
    showUploadError(err.message);
  }
}

document.getElementById("pasteSubmitBtn").addEventListener("click", async () => {
  hideUploadError();
  const text = document.getElementById("pasteTextarea").value;
  if (!text.trim()) {
    showUploadError("Please paste some policy text first.");
    return;
  }
  try {
    const data = await apiPost("/paste", {
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ text }),
    });
    showUploadResult(data);
    applyLoadedResponse(data);
    showView("analysis");
  } catch (err) {
    showUploadError(err.message);
  }
});

function showUploadResult(data) {
  const el = document.getElementById("uploadResult");
  el.classList.remove("hidden");
  el.innerHTML = `
    <div class="upload-result-title">✓ ${escapeHtml(data.documentName)} loaded successfully</div>
    <div class="upload-result-meta">${escapeHtml(data.documentType)} · ${data.chunkCount} sections extracted</div>
    ${!data.looksLikePrivacyPolicy ? `<div class="warning-banner">${escapeHtml(data.warning)}</div>` : ""}
    <div class="evidence-label">EXTRACTED TEXT PREVIEW</div>
    <div class="preview-text">${escapeHtml(data.preview)}</div>
    <div class="upload-result-actions">
      <button class="btn btn-primary btn-small" onclick="showView('analysis')">View Analysis</button>
      <button class="btn btn-secondary btn-small" onclick="showView('chat')">Ask PrivacyLens</button>
    </div>
  `;
}

function showUploadError(message) {
  const el = document.getElementById("uploadError");
  el.textContent = message;
  el.classList.remove("hidden");
}
function hideUploadError() {
  document.getElementById("uploadError").classList.add("hidden");
}

// ---------------- Analysis page ----------------

async function loadAnalysis(summaryOnly) {
  try {
    const data = await apiGet("/analysis");

    if (!summaryOnly) {
      renderFullAnalysis(data);
    }
    renderAnalysisSummary(data);
  } catch (err) {
    if (!summaryOnly) {
      document.getElementById("analysisContent").innerHTML =
        `<div class="error-banner">${escapeHtml(err.message)}</div>`;
    }
  }
}

function renderAnalysisSummary(data) {
  const el = document.getElementById("analysisSummaryList");
  if (!data.findings || data.findings.length === 0) {
    el.textContent = "No analysis available.";
    return;
  }
  el.innerHTML = data.findings.map((f) => `
    <div class="mini-status-row">
      <span>${escapeHtml(f.category)}</span>
      <span class="status-badge ${statusClass(f.status)}">${statusLabel(f.status)}</span>
    </div>
  `).join("");
}

function renderFullAnalysis(data) {
  const container = document.getElementById("analysisContent");

  if (!data.policyLoaded) {
    container.innerHTML = `<div class="empty-state">${escapeHtml(data.message || "No policy loaded.")}</div>`;
    return;
  }

  let html = "";
  if (data.message) {
    html += `<div class="warning-banner">${escapeHtml(data.message)}</div>`;
  }

  html += data.findings.map((f) => `
    <div class="analysis-card">
      <div class="analysis-card-header">
        <h3>${escapeHtml(f.category)}</h3>
        <span class="status-badge ${statusClass(f.status)}">${statusLabel(f.status)}</span>
      </div>
      <p class="analysis-explanation">${escapeHtml(f.explanation)}</p>
      ${f.evidence && f.evidence.length > 0 ? `
        <div class="evidence-label">EVIDENCE FROM POLICY</div>
        ${f.evidence.map((e) => `<div class="evidence-block">"${escapeHtml(e.text)}"</div>`).join("")}
      ` : ""}
    </div>
  `).join("");

  container.innerHTML = html;
}

// ---------------- Chat page ----------------

const chatWindow = document.getElementById("chatWindow");
const chatForm = document.getElementById("chatForm");
const chatInput = document.getElementById("chatInput");

document.querySelectorAll(".chip").forEach((chip) => {
  chip.addEventListener("click", () => {
    chatInput.value = chip.dataset.q;
    sendChatMessage();
  });
});

chatForm.addEventListener("submit", (e) => {
  e.preventDefault();
  sendChatMessage();
});

async function sendChatMessage() {
  const question = chatInput.value.trim();
  if (!question) return;

  appendQuestionBubble(question);
  chatInput.value = "";
  const typingEl = appendTypingIndicator();

  try {
    const data = await apiPost("/chat", {
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ question }),
    });
    typingEl.remove();
    appendAnswerBubble(data);
  } catch (err) {
    typingEl.remove();
    appendAnswerBubble({ answer: err.message, status: "NOT_CLEARLY_STATED", evidence: [] });
  }
}

function appendQuestionBubble(question) {
  const div = document.createElement("div");
  div.className = "chat-turn-question";
  div.textContent = question;
  chatWindow.appendChild(div);
  chatWindow.scrollTop = chatWindow.scrollHeight;
}

function appendTypingIndicator() {
  const div = document.createElement("div");
  div.className = "chat-turn-answer typing-indicator";
  div.textContent = "PrivacyLens is checking the policy...";
  chatWindow.appendChild(div);
  chatWindow.scrollTop = chatWindow.scrollHeight;
  return div;
}

function appendAnswerBubble(data) {
  const div = document.createElement("div");
  div.className = "chat-turn-answer";

  let html = `
    <div class="answer-section-label">ANSWER</div>
    <p class="answer-text">${escapeHtml(data.answer)}</p>
    <div class="answer-section-label">STATUS</div>
    <span class="status-badge ${statusClass(data.status)}">${statusLabel(data.status)}</span>
  `;

  if (data.evidence && data.evidence.length > 0) {
    html += `<div class="answer-section-label">EVIDENCE FROM POLICY</div>`;
    html += data.evidence.map((e) => `<div class="evidence-block">"${escapeHtml(e.text)}"</div>`).join("");
  }

  div.innerHTML = html;
  chatWindow.appendChild(div);
  chatWindow.scrollTop = chatWindow.scrollHeight;
}

// ---------------- Init: check if a policy is already loaded on the server ----------------

(async function init() {
  try {
    const status = await apiGet("/status");
    if (status.policyLoaded) {
      applyLoadedResponse({
        documentName: status.documentName,
        documentType: status.documentType,
        looksLikePrivacyPolicy: status.looksLikePrivacyPolicy,
        chunkCount: status.chunkCount,
        preview: "",
      });
    }
  } catch (err) {
    // Backend not reachable yet on first paint; ignore.
  }
})();
