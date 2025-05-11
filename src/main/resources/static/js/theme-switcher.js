function switchTheme() {
    const themeLink = document.getElementById("theme-style");
    const currentTheme = themeLink.getAttribute("href");

    if (currentTheme.includes("light.css")) {
        themeLink.setAttribute("href", "/css/dark.css");
    } else {
        themeLink.setAttribute("href", "/css/light.css");
    }
}