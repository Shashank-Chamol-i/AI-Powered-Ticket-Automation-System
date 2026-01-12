function register() {
    // 1. Get the input elements FIRST
    const firstNameInput = document.getElementById("firstName");
    const lastNameInput  = document.getElementById("lastName");
    const emailInput     = document.getElementById("email");
    const passwordInput  = document.getElementById("password");
    
    const msg = document.getElementById("message");

    // 2. Now read their values
    const firstName = firstNameInput.value.trim();
    const lastName  = lastNameInput.value.trim();
    const email     = emailInput.value.trim();
    const password  = passwordInput.value.trim();

    // 3. Validation
    if (!firstName || !lastName || !email || !password) {
        msg.className = "text-danger";
        msg.innerText = "All fields are required.";
        return;
    }

    // 4. Send request
    fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ firstName, lastName, email, password })
    })
    .then(res => res.json())
    .then(data => {
        msg.className = data.success ? "text-success" : "text-danger";
        msg.innerText = data.message || "Something went wrong";

        if (data.success) {
            setTimeout(() => {
                window.location.href = "index.html";
            }, 1500);
        }
    })
    .catch(err => {
        console.error(err);
        msg.className = "text-danger";
        msg.innerText = "Network error or server is not running";
    });
}