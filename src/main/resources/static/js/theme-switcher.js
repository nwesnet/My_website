// Load the preferred theme from server on every page
window.addEventListener("DOMContentLoaded", () => {
    fetch("/settings")
        .then(res => res.json())
        .then(data => {
            const theme = data.theme;
            const themeLink = document.getElementById("theme-style");
            if (theme === "light") {
                themeLink.setAttribute("href", "/css/light.css");
            } else {
                themeLink.setAttribute("href", "/css/dark.css");
            }
        })
        .catch(err => {
            console.error("Failed to load theme settings: ", err);
        });
});
