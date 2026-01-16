window.API_BASE_URL="https://ticket-auto-6wwn.onrender.com";
function saveToken(token) {
    localStorage.setItem("token", token);
}

function getToken() {
    return localStorage.getItem("token");
}

function parseJwt(token) {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
}

function checkAuth() {
    const token = getToken();

    if (!token) {
        redirectToLogin();
        return null;
    }

    try {
        const decoded = parseJwt(token);

        if (decoded.exp && decoded.exp * 1000 < Date.now()) {
            logout();
            return null;
        }

        return decoded;
    } catch {
        logout();
        return null;
    }
}

function logout() {
    localStorage.removeItem("token");
    redirectToLogin();
}

function redirectToLogin() {
    window.location.href = "../index.html";
}
