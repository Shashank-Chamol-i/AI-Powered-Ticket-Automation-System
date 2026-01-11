let agentsCache = [];

document.addEventListener("DOMContentLoaded", () => {
    const decoded = checkAuth();
    if (!decoded) return;

    document.getElementById("adminInfo").innerText = `Welcome Admin ${decoded.username}`;

    loadAgents()
        .then(() => loadUnassignedTickets())
        .catch(err => console.error(err));

    loadUsers();
});

// Load agents before tickets
function loadAgents() {
    return fetch("http://localhost:8080/api/ticket/agent", {
        headers: {
            "Authorization": "Bearer " + getToken()
        }
    })
    .then(res => {
        if (!res.ok) throw new Error("Failed to fetch agents");
        return res.json();
    })
    .then(data => {
        agentsCache = data;
    });
}

function loadUnassignedTickets() {
    fetch("http://localhost:8080/api/ticket/list", {
        headers: {
            "Authorization": "Bearer " + getToken()
        }
    })
    .then(res => {
        if (!res.ok) throw new Error("Failed to fetch tickets");
        return res.json();
    })
    .then(renderTickets)
    .catch(err => console.error(err));
}

function renderTickets(tickets) {
    const tbody = document.getElementById("ticketTableBody");
    if (!tbody) return;

    tbody.innerHTML = "";

    if (!tickets.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="3" class="text-center text-muted">
                    No unassigned open tickets
                </td>
            </tr>`;
        return;
    }

    tickets.forEach(ticket => {
        // Log ticket and agentsCache for debugging
        console.log("Rendering ticket:", ticket);
        console.log("Available agents:", agentsCache);

        const agentOptions = agentsCache.map(agent => {
            if (!agent.id) {
                console.warn("Agent with missing id detected", agent);
            }
            return `<option value="${agent.id}">${agent.firstName} (${agent.email})</option>`;
        }).join("");

        tbody.innerHTML += `
            <tr id="row-${ticket.id}">
                <td>${ticket.id}</td>
                <td>${ticket.title}</td>
                <td>
                    <div class="d-flex gap-2">
                        <select class="form-select form-select-sm">
                            <option value="">Select Agent</option>
                            ${agentOptions}
                        </select>
                        <button class="btn btn-sm btn-outline-primary"
                                onclick="assignTicket(this, '${ticket.id}')">
                            Assign
                        </button>
                    </div>
                </td>
            </tr>`;
    });
}


function assignTicket(btn, ticketId) {
    // Find the closest parent td element and then find the select inside it
    const td = btn.closest('td');
    if (!td) {
        alert("Unable to find select element container");
        return;
    }

    const select = td.querySelector('select');
    if (!select) {
        alert("Select element not found");
        return;
    }

    console.log("Select element:", select);
    console.log("Selected value:", select.value);

    const agentId = select.value;

    if (!agentId) {
        alert("Please select an agent");
        return;
    }

    fetch(`http://localhost:8080/api/ticket/assign?TID=${ticketId}&AID=${agentId}`, {
        method: "POST",
        headers: { "Authorization": "Bearer " + getToken() }
    })
    .then(res => {
        if (!res.ok) throw new Error("Assignment failed");
        document.getElementById(`row-${ticketId}`)?.remove();
    })
    .catch(err => alert(err.message));
}


// USERS LOADING

function loadUsers() {
    fetch("http://localhost:8080/api/ticket/get/list", {
        headers: {
            "Authorization": "Bearer " + getToken()
        }
    })
    .then(res => {
        if (!res.ok) throw new Error("Failed to fetch users");
        return res.json();
    })
    .then(renderUsers)
    .catch(err => console.error(err));
}

function renderUsers(users) {
    const tbody = document.getElementById("userTableBody");
    if (!tbody) return;

    tbody.innerHTML = "";

    users.forEach(user => {
        const normalizedRole = (user.role || "").toUpperCase().replace("ROLE_", "");

        let actionBtn = "-";
        if (normalizedRole === "USER") {
            actionBtn = `
                <button
                    class="btn btn-sm btn-outline-primary"
                    onclick="createAgent('${user.id}')">
                    Make Agent
                </button>
            `;
        }

        tbody.innerHTML += `
            <tr>
                <td>${user.id}</td>
                <td>${user.firstName}</td>
                <td>${user.email}</td>
                <td>
                    <span class="badge bg-${roleColor(normalizedRole)}">
                        ${normalizedRole}
                    </span>
                </td>
                <td class="text-center">
                    ${actionBtn}
                </td>
            </tr>`;
    });
}

function roleColor(role) {
    switch ((role || "").toUpperCase()) {
        case "ADMIN": return "danger";
        case "SUPPORT_AGENT": return "success";
        case "USER": return "secondary";
        default: return "secondary";
    }
}

function createAgent(userId) {
    if (!confirm("Are you sure you want to promote this user to Support Agent?")) return;

    fetch(`http://localhost:8080/api/ticket/create/agent?userId=${userId}`, {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + getToken()
        }
    })
    .then(res => {
        if (!res.ok) throw new Error("Failed to create agent");
        return res.json(); // You can also ignore this if backend returns nothing
    })
    .then(() => {
        // Reload both users and agents cache so assignment dropdown updates
        loadAgents().then(() => loadUsers());
    })
    .catch(err => alert(err.message));
}
