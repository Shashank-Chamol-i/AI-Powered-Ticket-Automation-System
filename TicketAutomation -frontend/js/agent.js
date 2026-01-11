// ===============================
// AUTO PAGE RELOAD EVERY 2 SECONDS
// ===============================

// ===============================
// GLOBAL STATE
// ===============================
let summaryModal;
let messageModal;

let activeTicketId = null;
let activeSummaryTicketId = null;

let chatInterval = null;
let summaryInterval = null;

// ===============================
// INIT
// ===============================
document.addEventListener("DOMContentLoaded", () => {

    const decoded = checkAuth();
    if (!decoded) return;

    document.getElementById("agentInfo").innerText =
        `Welcome Agent ${decoded.username}`;

    summaryModal = new bootstrap.Modal(
        document.getElementById("summaryModal")
    );

    messageModal = new bootstrap.Modal(
        document.getElementById("messageModal")
    );

    // CLEANUP WHEN MESSAGE MODAL CLOSES
    document.getElementById("messageModal")
        .addEventListener("hidden.bs.modal", () => {

            activeTicketId = null;
            clearInterval(chatInterval);

            document.getElementById("chatMessages").innerHTML = "";
            document.getElementById("chatInput").value = "";
            document.getElementById("chatInput").disabled = true;
            document.getElementById("sendBtn").disabled = true;

            removeWaitingLabel();
        });

    // CLEANUP WHEN SUMMARY MODAL CLOSES
    document.getElementById("summaryModal")
        .addEventListener("hidden.bs.modal", () => {
            clearInterval(summaryInterval);
            activeSummaryTicketId = null;
        });

    loadAssignedTickets();
});

// ===============================
// LOAD TICKETS
// ===============================
function loadAssignedTickets() {

    fetch("http://localhost:8080/api/ticket/assignTicket", {
        headers: { "Authorization": "Bearer " + getToken() }
    })
    .then(res => res.json())
    .then(renderTickets)
    .catch(console.error);
}

// ===============================
// RENDER TICKETS
// ===============================
function renderTickets(tickets) {

    const tbody = document.getElementById("ticketTableBody");
    tbody.innerHTML = "";

    if (!tickets?.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center text-muted">
                    No tickets assigned
                </td>
            </tr>`;
        return;
    }

    tickets.forEach(t => {

        const canResolve = t.status === "IN_PROGRESS";
        const canMessage =
            t.status === "IN_PROGRESS" ||
            t.status === "WAITING_FOR_CUSTOMER";

        tbody.innerHTML += `
        <tr>
            <td>${t.id}</td>
            <td>${t.title}</td>
            <td><span class="badge bg-secondary">${t.status}</span></td>
            <td>${t.priority || "-"}</td>
            <td class="d-flex gap-2">

                <button class="btn btn-sm btn-outline-info"
                    onclick="viewSummary('${t.id}')">
                    AI Summary
                </button>

                ${canMessage ? `
                    <button class="btn btn-sm btn-outline-primary"
                        onclick="openMessageModal('${t.id}','${t.status}')">
                        Message
                    </button>` : ""}

                ${canResolve ? `
                    <button class="btn btn-sm btn-success"
                        onclick="resolveTicket('${t.id}')">
                        Resolve
                    </button>` : ""}

                <select class="form-select form-select-sm"
                    onchange="assignPriority('${t.id}', this.value)">
                    <option value="">Set Priority</option>
                    <option>LOW</option>
                    <option>MEDIUM</option>
                    <option>HIGH</option>
                    <option>CRITICAL</option>
                </select>

            </td>
        </tr>`;
    });
}

// ===============================
// AI SUMMARY
// ===============================
function viewSummary(ticketId) {

    activeSummaryTicketId = ticketId;
    fetchSummary(ticketId);

    clearInterval(summaryInterval);
    summaryInterval = setInterval(() => {
        if (activeSummaryTicketId) {
            fetchSummary(activeSummaryTicketId);
        }
    }, 25000);

    summaryModal.show();
}

function fetchSummary(ticketId) {

    fetch(`http://localhost:8080/api/analysis/summary?TID=${ticketId}`, {
        headers: { "Authorization": "Bearer " + getToken() }
    })
    .then(res => res.json())
    .then(data => {
        document.getElementById("aiSummary").innerText =
            data.summary || "-";
        document.getElementById("predictedCategory").innerText =
            data.predictedCategory || "-";
        document.getElementById("predictedPriority").innerText =
            data.predictedPriority || "-";
    });
}

// ===============================
// ASSIGN PRIORITY
// ===============================
function assignPriority(ticketId, priority) {

    if (!priority) return;

    fetch("http://localhost:8080/api/ticket/agent/action", {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + getToken(),
            "TID": ticketId,
            "PRI": priority
        }
    })
    .then(loadAssignedTickets);
}

// ===============================
// RESOLVE TICKET
// ===============================
function resolveTicket(ticketId) {

    if (!confirm("Resolve this ticket?")) return;

    fetch("http://localhost:8080/api/ticket/agent/resolve", {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + getToken(),
            "TID": ticketId
        }
    })
    .then(loadAssignedTickets);
}

// ===============================
// MESSAGE MODAL
// ===============================
function openMessageModal(ticketId, status) {

    activeTicketId = ticketId;

    document.getElementById("chatMessages").innerHTML = "";
    document.getElementById("chatInput").value = "";

    const canSend = status === "IN_PROGRESS";
    document.getElementById("chatInput").disabled = !canSend;
    document.getElementById("sendBtn").disabled = !canSend;

    removeWaitingLabel();
    if (!canSend) showWaitingLabel();

    loadConversation(ticketId);

    clearInterval(chatInterval);
    chatInterval = setInterval(() => {
        if (activeTicketId) loadConversation(activeTicketId);
    }, 7000);

    messageModal.show();
}

// ===============================
// LOAD CONVERSATION
// ===============================
function loadConversation(ticketId) {

    fetch(`http://localhost:8080/api/message/conversation?TID=${ticketId}`, {
        headers: { "Authorization": "Bearer " + getToken() }
    })
    .then(res => res.json())
    .then(renderConversation);
}

// ===============================
// RENDER CHAT
// ===============================
function renderConversation(messages) {

    const box = document.getElementById("chatMessages");
    box.innerHTML = "";

    messages.forEach(m => {

        const isAgent = m.senderType === "SUPPORT_AGENT";

        box.innerHTML += `
        <div class="d-flex ${isAgent ? "justify-content-start" : "justify-content-end"} mb-2">
            <div class="p-2 rounded
                ${isAgent ? "bg-primary text-white" : "bg-light"}"
                style="max-width:70%">
                ${m.message}
                <small class="d-block text-end opacity-75">
                    ${new Date(m.sendAt).toLocaleString()}
                </small>
            </div>
        </div>`;
    });

    box.scrollTop = box.scrollHeight;
}

// ===============================
// SEND MESSAGE
// ===============================
function sendAgentMessage() {

    const msg = document.getElementById("chatInput").value.trim();
    if (!msg || !activeTicketId) return;

    fetch(`http://localhost:8080/api/message/agent/send?TID=${activeTicketId}`, {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + getToken(),
            "Content-Type": "text/plain"
        },
        body: msg
    })
    .then(res => res.json())
    .then(() => {
        document.getElementById("chatInput").disabled = true;
        document.getElementById("sendBtn").disabled = true;
        showWaitingLabel();
        loadConversation(activeTicketId);
        loadAssignedTickets();
    });
}

// ===============================
// WAITING LABEL
// ===============================
function showWaitingLabel() {

    const footer = document.querySelector("#messageModal .modal-footer");

    if (!document.getElementById("waitingStatus")) {
        footer.insertAdjacentHTML("afterbegin", `
            <div id="waitingStatus" class="text-danger fw-semibold me-auto">
                Waiting for customer response
            </div>`);
    }
}

function removeWaitingLabel() {
    document.getElementById("waitingStatus")?.remove();
}
