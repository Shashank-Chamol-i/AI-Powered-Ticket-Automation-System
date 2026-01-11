window.onload = function () {
    const decoded = checkAuth();
    if (!decoded) return;

    const userInfoEl = document.getElementById("userInfo");
    if (userInfoEl) {
        userInfoEl.innerText = `Welcome ${decoded.username}`;
    }
};
