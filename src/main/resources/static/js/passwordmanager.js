document.addEventListener("DOMContentLoaded", () => {
    const mainImage = document.getElementById("mainImage");
    const thumbnails = Array.from(document.querySelectorAll("#thumbnails img"));

    let currentIndex = 0;

    const imagePaths = thumbnails.map(img => img.src);

    function render() {
        mainImage.src = imagePaths[currentIndex];
    }

    window.nextImage = function() {
        currentIndex = (currentIndex + 1) % imagePaths.length;
        render();
    };
    window.previousImage = function() {
        currentIndex = (currentIndex - 1 + imagePaths.length) % imagePaths.length;
        render();
    };
    document.getElementById("thumbnails").addEventListener("click", (e) => {
        const clicked = e.target.closest("img");
        if(clicked) {
            const index = thumbnails.findIndex(img => img === clicked);
            if(index != -1) {
                currentIndex = index;
                render();
            }
        }
    });
    window.downloadSelected = function() {
        const version = document.getElementById("versionSelector").value;
        if(version && version !== "Select version (placeholder)") {
            window.location.href = `/download/passwordmanager/${version}`;
        } else {
            alert("Please select a version to download.");
        }
    };
});