let activeChatTicketId = null;
let messageLocked = false;

// ===============================
// OPEN MESSAGE MODAL
// ===============================
function openMessageModal(ticketId, ticketStatus) {

    if (ticketStatus !== "WAITING_FOR_CUSTOMER") {
        alert("Messaging is available only when ticket is waiting for customer.");
        return;
    }

    activeChatTicketId = ticketId;
    messageLocked = false;

    document.getElementById("messageModal").classList.add("show");
    document.getElementById("messageModal").style.display = "block";
    document.body.classList.add("modal-open");

    document.getElementById("chatInput").disabled = false;
    document.getElementById("chatInput").value = "";
    document.getElementById("sendBtn").disabled = false;

    removeWaitingStatus();

    loadConversation(ticketId);
}

// ===============================
// CLOSE MESSAGE MODAL
// ===============================
function closeMessageModal() {
    document.getElementById("messageModal").classList.remove("show");
    document.getElementById("messageModal").style.display = "none";
    document.body.classList.remove("modal-open");

    document.getElementById("chatMessages").innerHTML = "";
    activeChatTicketId = null;
    messageLocked = false;

    removeWaitingStatus();
}

// ===============================
// LOAD CONVERSATION
// ===============================
function loadConversation(ticketId) {

    fetch(`${window.API_BASE_URL}/api/message/conversation?TID=${ticketId}`, {
        headers: authHeader()
    })
    .then(res => res.json())
    .then(messages => renderConversation(messages));
}

// ===============================
// RENDER CONVERSATION
// ===============================
function renderConversation(messages) {

    const container = document.getElementById("chatMessages");
    container.innerHTML = "";

    if (!messages || messages.length === 0) {
        container.innerHTML =
            `<p class="text-muted text-center">No messages yet.</p>`;
        return;
    }

    messages.forEach(renderSingleMessage);
    container.scrollTop = container.scrollHeight;
}

// ===============================
// RENDER SINGLE MESSAGE
// ===============================
function renderSingleMessage(msg) {

    const container = document.getElementById("chatMessages");

    const isUser = msg.senderType === "USER";
    const alignClass = isUser ? "justify-content-end" : "justify-content-start";
    const bubbleClass = isUser ? "bg-primary text-white" : "bg-light";
    const time = new Date(msg.sendAt).toLocaleString();

    container.innerHTML += `
        <div class="d-flex ${alignClass} mb-2">
            <div class="p-2 rounded ${bubbleClass}" style="max-width:70%;">
                <div>${msg.message}</div>
                <small class="d-block text-end opacity-75">${time}</small>
            </div>
        </div>
    `;
}

// ===============================
// SEND MESSAGE
// ===============================
function sendMessage() {

    if (messageLocked) return;

    const input = document.getElementById("chatInput");
    const message = input.value.trim();

    if (!message || !activeChatTicketId) return;

    fetch(`${window.API_BASE_URL}/api/message/user/send`, {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("token"),
            "TID": activeChatTicketId,
            "Content-Type": "text/plain"
        },
        body: message
    })
    .then(res => res.json())
    .then(response => {

        renderSingleMessage(response);

        lockMessaging();

        document.getElementById("chatMessages").scrollTop =
            document.getElementById("chatMessages").scrollHeight;
    });
}

// ===============================
// LOCK MESSAGING (WAITING STATE)
// ===============================
function lockMessaging() {

    messageLocked = true;

    document.getElementById("chatInput").disabled = true;
    document.getElementById("sendBtn").disabled = true;

    showWaitingStatus();
}

// ===============================
// WAITING STATUS UI
// ===============================
function showWaitingStatus() {

    if (document.getElementById("waitingStatus")) return;

    const footer = document.querySelector(".modal-footer");

    const status = document.createElement("div");
    status.id = "waitingStatus";
    status.className = "w-100 text-center mt-2 text-danger fw-semibold";
    status.innerText = "Waiting for Agent Response";

    footer.appendChild(status);
}

function removeWaitingStatus() {
    const status = document.getElementById("waitingStatus");
    if (status) status.remove();
}
