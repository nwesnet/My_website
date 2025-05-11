document.addEventListener("DOMContentLoaded", () => {
    class Gallery {
        constructor(mainId, imagePaths) {
            this.mainImage = document.getElementById(mainId);
            this.images = imagePaths;
            this.index = 0;
            this.render();
        }

        render() {
            this.mainImage.src = this.images[this.index];
        }

        changeImage(src) {
            const path = new URL(src, window.location.href).pathname;
            const found = this.images.indexOf(path);
            if (found !== -1) this.index = found;
            this.mainImage.src = path;
        }

        next() {
            this.index = (this.index + 1) % this.images.length;
            this.render();
        }

        previous() {
            this.index = (this.index - 1 + this.images.length) % this.images.length;
            this.render();
        }
    }

    const gallery1 = new Gallery("mainImage1", [
        "/img/PasswordmanagerImg/PasswordManagerIng.png",
        "/img/PasswordmanagerImg/LoginPage.png",
        "/img/PasswordmanagerImg/AddDataPage.png",
        "/img/PasswordmanagerImg/GeneratePasswordPage.png",
        "/img/PasswordmanagerImg/SecuritySettings.png",
        "/img/PasswordmanagerImg/MainPage.png"
    ]);

    const gallery2 = new Gallery("mainImage2", [
        "/img/DictionaryImg/Iwantit.png",
        "/img/DictionaryImg/IwantitFirst.png",
        "/img/DictionaryImg/ooomy.png",
        "/img/DictionaryImg/bats.png",
        "/img/DictionaryImg/beautiful.png"
    ]);

    [
        { strip: "thumbnails1", gallery: gallery1 },
        { strip: "thumbnails2", gallery: gallery2 }
    ].forEach(({ strip, gallery }) => {
        document.getElementById(strip).addEventListener("click", (e) => {
            const img = e.target.closest("img");
            if (img) gallery.changeImage(img.src);
        });
    });

    window.changeImage  = (n, src) => (n === 1 ? gallery1 : gallery2).changeImage(src);
    window.nextImage    = (n)     => (n === 1 ? gallery1 : gallery2).next();
    window.previousImage= (n)     => (n === 1 ? gallery1 : gallery2).previous();
});
