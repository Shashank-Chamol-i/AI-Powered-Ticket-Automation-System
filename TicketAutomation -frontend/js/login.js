function login() {
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();
    const error = document.getElementById("error");

    if (!email || !password) {
        error.innerText = "Email and password required.";
        return;
    }

    fetch(`http://localhost:8080/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
    })
    .then(res => {
        if (!res.ok) throw new Error("Invalid credentials");
        return res.json();
    })
    .then(data => {
        saveToken(data.token);

        const decoded = parseJwt(data.token);
        const role = decoded.role || (decoded.roles && decoded.roles[0]);

        if (role === "ROLE_ADMIN") location.href = "dashboard/admin.html";
        else if (role === "ROLE_SUPPORT_AGENT") location.href = "dashboard/agent.html";
        else location.href = "dashboard/user.html";
    })
    .catch(err => error.innerText = err.message);
}
