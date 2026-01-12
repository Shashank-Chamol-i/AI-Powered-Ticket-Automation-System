let summaryInterval = null;
let currentTicketId = null;

// ===============================
// AUTH HEADER
// ===============================
function authHeader() {
    return {
        "Authorization": "Bearer " + localStorage.getItem("token"),
        "Content-Type": "application/json"
    };
}

// ===============================
// SECTION TOGGLING
// ===============================
function showSection(sectionId) {
    const sections = [
        "ticketListSection",
        "ticketDetailSection",
        "createTicketSection"
    ];

    sections.forEach(id => document.getElementById(id).style.display = "none");
    document.getElementById("searchFilterSection").style.display =
        sectionId === "ticketListSection" ? "block" : "none";
    document.getElementById(sectionId).style.display = "block";

    if (sectionId !== "ticketDetailSection" && summaryInterval) {
        clearInterval(summaryInterval);
        summaryInterval = null;
    }
}

// ===============================
// LOAD ALL USER TICKETS
// ===============================
function loadUserTickets() {
    showSection("ticketListSection");

    fetch("http://localhost:8080/api/ticket/user", { headers: authHeader() })
        .then(res => res.json())
        .then(renderTicketList)
        .catch(err => alert("No Tickets Created Yet : "));
}

// ===============================
// RENDER TICKET LIST
// ===============================
function renderTicketList(tickets) {
    const container = document.getElementById("ticketList");
    container.innerHTML = "";

    if (!tickets || tickets.length === 0) {
        container.innerHTML = `<p class="text-muted">No tickets found.</p>`;
        return;
    }

    tickets.forEach(ticket => {
        const canDelete = ticket.status === "OPEN";

        container.innerHTML += `
            <div class="card shadow-sm mb-3">
                <div class="card-body">
                    <h6>${ticket.title}</h6>
                    <p class="text-muted small">${ticket.description}</p>
                    <div class="d-flex justify-content-between align-items-center">
                        <small><strong>ID:</strong> ${ticket.id}</small>
                        <div class="d-flex gap-2">
                            <button class="btn btn-sm btn-outline-primary"
                                onclick="viewTicketDetails('${ticket.id}')">View Details</button>

                            ${canDelete ? `<button class="btn btn-sm btn-outline-danger"
                                onclick="confirmDelete('${ticket.id}')">Delete</button>` : ""}
                        </div>
                    </div>
                </div>
            </div>`;
    });
}

// ===============================
// CONFIRM DELETE
// ===============================
function confirmDelete(ticketId) {
    if (!confirm("Are you sure you want to delete this ticket?")) return;

    fetch(`http://localhost:8080/api/ticket/${ticketId}`, {
        method: "DELETE",
        headers: authHeader()
    })
        .then(res => {
            if (!res.ok) throw new Error("Delete failed");
            loadUserTickets();
        })
        .catch(err => alert(err.message));
}

// ===============================
// VIEW TICKET DETAILS (UPDATED)
// ===============================
function viewTicketDetails(ticketId) {
    currentTicketId = ticketId;
    showSection("ticketDetailSection");

    fetch(`http://localhost:8080/api/ticket/${ticketId}`, { headers: authHeader() })
        .then(res => res.json())
        .then(ticket => {
            const isClosed = ticket.status === "CLOSED";
            const canClose = ticket.status === "RESOLVED";

            document.getElementById("detailContent").innerHTML = `
                <h5>${ticket.title}</h5>
                <p class="text-muted">${ticket.description}</p>

                <div class="d-flex gap-2 mb-3">
                    <button class="btn btn-outline-secondary"
                        onclick="openMessageModal('${ticket.id}', '${ticket.status}')">
                        Message Agent
                    </button>
                </div>

                <hr>

                <div class="row small align-items-center">
                    <div class="col-md-6"><strong>Status:</strong> ${ticket.status}</div>
                    <div class="col-md-6"><strong>Priority:</strong> ${ticket.priority ?? "Not Assigned"}</div>
                    <div class="col-md-6"><strong>Category:</strong> ${ticket.category}</div>
                    <div class="col-md-6"><strong>Assigned To:</strong> ${ticket.assignedTo ?? "Unassigned"}</div>
                    <div class="col-md-6"><strong>Created:</strong> ${new Date(ticket.createdAt).toLocaleString()}</div>

                    <div class="col-md-6 d-flex justify-content-between align-items-center">
                        <span><strong>Resolved:</strong> ${
                            ticket.resolvedAt
                                ? new Date(ticket.resolvedAt).toLocaleString()
                                : "Not Resolved"
                        }</span>

                        ${canClose ? `<button class="btn btn-sm btn-success"
                            onclick="confirmCloseTicket('${ticket.id}')">Close</button>` : ""}
                    </div>
                </div>

                ${isClosed
                    ? `<div class="alert alert-secondary mt-4 mb-0">
                        This ticket is closed and is now read-only.
                       </div>`
                    : ""}
            `;

            if (!isClosed) {
                fetchSummary();
                summaryInterval = setInterval(fetchSummary, 10000);
            } else if (summaryInterval) {
                clearInterval(summaryInterval);
                summaryInterval = null;
            }
        })
        .catch(err => alert("Failed to load ticket details"));
}

// ===============================
// CONFIRM CLOSE TICKET
// ===============================
function confirmCloseTicket(ticketId) {
    if (!confirm("Do you want to close this ticket?")) return;

    fetch(`http://localhost:8080/api/ticket/user/close?TID=${ticketId}`, {
        method: "POST",
        headers: authHeader()
    })
        .then(res => res.json())
        .then(() => viewTicketDetails(ticketId))
        .catch(err => alert("Failed to close ticket"));
}

// ===============================
// SEARCH TICKET BY ID
// ===============================
function searchTicketById() {
    const ticketId = document.getElementById("searchTicketId").value.trim();
    if (!ticketId) return;

    fetch(`http://localhost:8080/api/ticket/${ticketId}`, { headers: authHeader() })
        .then(res => {
            if (!res.ok) throw new Error("Ticket not found");
            return res.json();
        })
        .then(ticket => renderTicketList([ticket]))
        .catch(err => alert(err.message));
}

// ===============================
// FILTER & RESET TICKETS
// ===============================
function applyFilters() {
    const query = ["status", "category", "priority"]
        .map(id => {
            const val = document.getElementById(`filter${id.charAt(0).toUpperCase() + id.slice(1)}`).value;
            return val ? `${id}=${val}` : null;
        })
        .filter(Boolean)
        .join("&");

    fetch(`http://localhost:8080/api/ticket/filter?${query}`, { headers: authHeader() })
        .then(res => res.json())
        .then(renderTicketList)
        .catch(err => alert("Failed to apply filters"));
}

function resetFilters() {
    ["searchTicketId", "filterStatus", "filterCategory", "filterPriority"].forEach(id => {
        document.getElementById(id).value = "";
    });
    loadUserTickets();
}

// ===============================
// AI SUMMARY
// ===============================
function fetchSummary() {
    if (!currentTicketId) return;

    fetch(`http://localhost:8080/api/analysis/summary?TID=${currentTicketId}`, {
        headers: authHeader()
    })
        .then(res => res.json())
        .then(data => {
            document.getElementById("aiSummary").innerText =
                data.summary || "No summary available";
        })
        .catch(err => console.error("Failed to fetch AI summary"));
}
// ===============================
// CREATE TICKET
// ===============================
function createTicket() {
    const title = document.getElementById("title").value.trim();
    const description = document.getElementById("description").value.trim();
    const category = document.getElementById("category").value;
    const msg = document.getElementById("createTicketMsg");

    if (!title || !description) {
        msg.className = "text-danger";
        msg.innerText = "Title and Description are required.";
        return;
    }

    fetch("http://localhost:8080/api/ticket/create", {
        method: "POST",
        headers: authHeader(),
        body: JSON.stringify({ title, description, category })
    })
    .then(res => {
        if (!res.ok) throw new Error("Ticket creation failed");
        return res.json();
    })
    .then(() => {
        msg.className = "text-success";
        msg.innerText = "Ticket created successfully.";
        document.getElementById("createTicketForm").reset();
    })
    .catch(err => {
        msg.className = "text-danger";
        msg.innerText = err.message;
    });
}