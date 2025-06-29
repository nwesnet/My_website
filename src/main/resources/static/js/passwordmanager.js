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
    const downloadBtn = document.getElementById("downloadBtn");
    if (downloadBtn) {
        downloadBtn.addEventListener("click", () => {
            const select = document.getElementById("versionSelector")
            const selectedValue = select.value;
            if (!selectedValue || selectedValue === "Select version") {
                alert("Please select a version first");
                return;
            }
            window.location.href = `/downloads/${selectedValue}`;
        })
    }
});