function register() {
    const firstName = firstName.value.trim();
    const lastName = lastName.value.trim();
    const email = email.value.trim();
    const password = password.value.trim();
    const msg = document.getElementById("message");

    if (!firstName || !lastName || !email || !password) {
        msg.className = "text-danger";
        msg.innerText = "All fields required.";
        return;
    }

    fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ firstName, lastName, email, password })
    })
    .then(res => res.json())
    .then(data => {
        msg.className = data.success ? "text-success" : "text-danger";
        msg.innerText = data.message;
        if (data.success) setTimeout(() => location.href = "index.html", 1500);
    });
}
