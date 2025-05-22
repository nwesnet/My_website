window.onload = function () {
    // ✅ Apply saved theme
        fetch("/settings")
            .then(response => response.json())
            .then(data => {
                const theme = data.theme;
                const themeLink = document.getElementById("theme-style");
                if (theme === "light") {
                    themeLink.href = "/css/light.css";
                } else {
                    themeLink.href = "/css/dark.css";
                }
            });
    const middleContent = document.getElementById("middleContent");

    // Handle sidebar button clicks
    let activeButton = null;

    document.querySelectorAll(".sideButton").forEach(btn => {
        btn.addEventListener("click", () => {
            const action = btn.textContent.trim();

            // Skip Logs toggle logic (handled separately)
            if (btn.id === "logsToggle") return;

            // If same button is clicked again, toggle off
            if (activeButton === btn) {
                middleContent.innerHTML = "";
                activeButton.classList.remove("active");
                activeButton = null;
            } else {
                if (activeButton) activeButton.classList.remove("active");
                btn.classList.add("active");
                activeButton = btn;

                // Handle rendering based on text
                if (action === "Add") {
                    renderAddForm("Account");
                } else if (action === "Generate Password") {
                    renderGeneratePasswordUI();
                } else if (action === "Preferences") {
                    renderPreferencesUI();
                }else if(action === "Show List") {
                    renderShowListUI();
                } else {
                    renderPlaceholder(action + "...");
                }
            }
        });
    });


    // Show placeholder text for non-Add buttons
    function renderPlaceholder(text) {
        middleContent.innerHTML = `<h2>${text}</h2>`;
    }

    // Render the "Add" form structure
    function renderAddForm(defaultType) {
        const formWrapper = document.createElement("div");
        formWrapper.innerHTML = `
            <h2>Add Information</h2>
            <label for="entryType" class="form-label">Select type:</label>
            <select id="entryType" class="form-select">
                <option value="Account" ${defaultType === "Account" ? "selected" : ""}>Account</option>
                <option value="Card">Card</option>
                <option value="Link">Link</option>
                <option value="Wallet">Wallet</option>
            </select>

            <div id="formContent" style="margin-top: 20px;"></div>

            <div style="margin-top: 20px;">
                <button id="saveAccountBtn" class="form-button">Save</button>
                <button class="form-button" onclick="document.getElementById('middleContent').innerHTML = ''">Cancel</button>
            </div>
        `;

        middleContent.innerHTML = '';
        middleContent.appendChild(formWrapper);
        renderFormFields(defaultType);
        document.getElementById("saveAccountBtn").addEventListener("click", () => {
            const entryType = document.getElementById("entryType").value;
            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
            const formInputs = document.querySelectorAll("#formContent .form-input");

            let url = "";
            let body = "";

            if (entryType === "Account") {
                const resource = formInputs[0].value;
                const username = formInputs[1].value;
                const password = formInputs[2].value;

                url = "/account/add-account";
                body = `resource=${encodeURIComponent(resource)}&username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`;
            }

            else if (entryType === "Card") {
                const resource = formInputs[0].value;
                const cardNumber = formInputs[1].value;
                const expiryDate = formInputs[2].value;
                const ownerName = formInputs[3].value;
                const cvv = formInputs[4].value;
                const cardPin = formInputs[5].value;
                const cardNetwork = formInputs[6].value;
                const cardType = formInputs[7].value;

                url = "/account/add-card";
                body = `resource=${encodeURIComponent(resource)}&cardNumber=${encodeURIComponent(cardNumber)}&expiryDate=${encodeURIComponent(expiryDate)}&ownerName=${encodeURIComponent(ownerName)}&cvv=${encodeURIComponent(cvv)}&cardPin=${encodeURIComponent(cardPin)}&cardNetwork=${encodeURIComponent(cardNetwork)}&cardType=${encodeURIComponent(cardType)}`;
            }
            else if (entryType === "Link") {
                const resource = formInputs[0].value;
                const linkURL = formInputs[1].value;

                url = "/account/add-link";
                body = `resource=${encodeURIComponent(resource)}&linkURL=${encodeURIComponent(linkURL)}`;
            }
            else if (entryType === "Wallet") {
                const resource = formInputs[0].value;
                const keyWords = document.querySelector("#formContent textarea").value;
                const address = formInputs[1].value;
                const password = formInputs[2].value;

                url = "/account/add-wallet";
                body = `resource=${encodeURIComponent(resource)}&keyWords=${encodeURIComponent(keyWords)}&address=${encodeURIComponent(address)}&password=${encodeURIComponent(password)}`;
            }

            // Add more types here (Link, Wallet) as needed...

            if (!url || !body) {
                alert("Unsupported type.");
                return;
            }

            fetch(url, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    [csrfHeader]: csrfToken
                },
                body: body
            })
            .then(response => response.text())
            .then(data => {
                if (data === "OK") {
                    alert(`${entryType} saved successfully.`);
                    document.getElementById("middleContent").innerHTML = "";
                } else {
                    alert("Error: " + data);
                }
            })
            .catch(error => {
                console.error("Error saving entry:", error);
                alert("Failed to save.");
            });
        });




        document.getElementById("entryType").addEventListener("change", function () {
            renderFormFields(this.value);
        });
    }

    // Form content for each type
    function renderFormFields(type) {
        const formContent = document.getElementById("formContent");
        let html = '';

        const input = (label, type = "text", placeholder = "") =>
            `<label class="form-label">${label}:</label><input class="form-input" type="${type}" placeholder="${placeholder}"><br>`;

        if (type === "Account") {
            html = input("Resource") + input("Login") + input("Password", "password");
        } else if (type === "Card") {
            html = input("Resource") +
                   input("Number") +
                   input("Date", "text", "MM/YY") +
                   input("Owner") +
                   input("CVV") +
                   input("PIN") +
                   input("Pay Network") +
                   input("Card Type");
        } else if (type === "Link") {
            html = input("Resource") + input("Link");
        } else if (type === "Wallet") {
            html = input("Resource") +
                   `<label class="form-label">Key Words:</label><textarea class="form-textarea" placeholder="Enter 8-32 words"></textarea><br>` +
                   input("Address") +
                   input("Password", "password");
        }

        formContent.innerHTML = html;
    }

    function renderGeneratePasswordUI() {
        middleContent.innerHTML = `
            <h2>Generate Password</h2>

            <div class="gen-group">
                <label>Password Length:</label>
                <input type="range" id="sliderLength" min="4" max="24" value="12">
                <input type="number" id="sliderValue" class="form-input small" value="12" min="4" max="24">
            </div>

            <div class="gen-options">
                <label><input type="checkbox" id="cbNumbers"> Numbers: 0-9</label><br>
                <label><input type="checkbox" id="cbUppercase"> Uppercase: A-Z</label><br>
                <label><input type="checkbox" id="cbLowercase"> Lowercase: a-z</label><br>
                <label><input type="checkbox" id="cbSymbols"> Symbols: ~!@#$%^&*()</label><br>
                <label><input type="checkbox" id="cbCustom"> Custom:
                    <input type="text" id="customSymbolsField" class="form-input small" placeholder="!@#\$%" disabled>
                </label>
            </div>

            <div class="gen-output">
                <input type="text" id="passwordOutput" class="form-input" readonly>
                <button class="form-button" id="generateBtn">Generate</button>
                <button class="form-button" id="copyBtn">Copy</button>
            </div>
        `;

        const slider = document.getElementById("sliderLength");
        const sliderValue = document.getElementById("sliderValue");
        const customSymbolsField = document.getElementById("customSymbolsField");
        const cbCustom = document.getElementById("cbCustom");

        // Sync slider with number field
        slider.addEventListener("input", () => {
            sliderValue.value = slider.value;
        });
        sliderValue.addEventListener("input", () => {
            slider.value = sliderValue.value;
        });

        // Enable/disable custom input
        cbCustom.addEventListener("change", () => {
            customSymbolsField.disabled = !cbCustom.checked;
        });

        // Generate password logic
        document.getElementById("generateBtn").addEventListener("click", () => {
            const length = parseInt(slider.value);
            const includeNumbers = document.getElementById("cbNumbers").checked;
            const includeUppercase = document.getElementById("cbUppercase").checked;
            const includeLowercase = document.getElementById("cbLowercase").checked;
            const includeSymbols = document.getElementById("cbSymbols").checked;
            const includeCustom = document.getElementById("cbCustom").checked;
            const customSymbols = document.getElementById("customSymbolsField").value;

            const result = generatePassword(length, includeNumbers, includeUppercase, includeLowercase, includeSymbols, includeCustom, customSymbols);
            document.getElementById("passwordOutput").value = result;
        });

        // Copy to clipboard
        document.getElementById("copyBtn").addEventListener("click", () => {
            const pass = document.getElementById("passwordOutput").value;
            if (pass) {
                navigator.clipboard.writeText(pass).then(() => alert("Copied!"));
            }
        });
    }

    // Actual password generation logic
    function generatePassword(length, numbers, upper, lower, symbols, custom, customStr) {
        let pool = '';
        if (numbers) pool += '0123456789';
        if (upper) pool += 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
        if (lower) pool += 'abcdefghijklmnopqrstuvwxyz';
        if (symbols) pool += '~`!@#$%^&*()_-+=[]{}/\\:;\"<>,.?|';
        if (custom && customStr) pool += customStr.replace(/\s+/g, '');

        if (!pool) return '';

        let result = '';
        for (let i = 0; i < length; i++) {
            result += pool.charAt(Math.floor(Math.random() * pool.length));
        }
        return result;
    }
    // Temporarily bypass logic
    let doubleConfirmBypassUtil = 0;
    function isDoubleConfirmationTemporarilyBypassed() {
        return Date.now() < doubleConfirmBypassUtil;
    }
    function enableTemporaryBypass(minutes = 1) {
        doubleConfirmBypassUtil = Date.now() + minutes * 60 * 1000;
    }
    let isDoubleConfirmationEnabled = false;
    function fetchDoubleConfirmationSettings() {
        fetch("/settings/get-double-confirmation")
            .then(res => res.json())
            .then(obj => { isDoubleConfirmationEnabled = !!obj.enabled; });
    }
    fetchDoubleConfirmationSettings();
    // Check double confirmation
    function withDoubleConfirmation(action) {
        if (!isDoubleConfirmationEnabled || isDoubleConfirmationTemporarilyBypassed()) {
            action();
        } else {
            showAdditionalPasswordDialog((enteredPwd, closeDialog) => {
                const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                fetch("/account/verify-additional-password", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        [csrfHeader]: csrfToken
                    },
                    body: JSON.stringify({ additionalPassword: enteredPwd })
                })
                .then(res => res.json())
                .then(data => {
                    if (data.ok) {
                        enableTemporaryBypass(1);
                        closeDialog();
                        action();
                    } else {
                        alert("Incorrect additional password");
                    }
                });
            });
        }
    }
    // ShowList logic
    let currentShowListType = "Account";
    let showListAllData = [];
    function renderShowListUI() {
        middleContent.innerHTML = `
            <div style="margin-bottom: 16px;">
                <input type="text" id="showListSearch" class="showlist-search" placeholder="Search...">
            </div>
            <div style="margin-bottom: 16px;">
                <button class="form-button tab-btn" data-type="Account">Accounts</button>
                <button class="form-button tab-btn" data-type="Card">Cards</button>
                <button class="form-button tab-btn" data-type="Link">Links</button>
                <button class="form-button tab-btn" data-type="Wallet">Wallets</button>
            </div>
            <div id="listContent"></div>
        `;
        currentShowListType = "Account";
        fetchAndDisplay(currentShowListType);
        // Tab click logic
        document.querySelectorAll(".tab-btn").forEach(btn => {
            btn.addEventListener("click", () => {
                const type = btn.getAttribute("data-type");
                currentShowListType = type;
                fetchAndDisplay(type);
            });
        });
        // Search bar logic
        const searchInput = document.getElementById("showListSearch");
        searchInput.addEventListener("input", () => {
            const query = searchInput.value.trim().toLowerCase();
            displayFilteredList(query);
        });
    }
    // Fetch and display, plus store the data for search
    function fetchAndDisplay(type) {
        let url = "";
        if (type === "Account") url = "/account/list-accounts";
        else if (type === "Card") url = "/account/list-cards";
        else if (type === "Link") url = "/account/list-links";
        else if (type === "Wallet") url = "/account/list-wallets";
        if (!url) return;

        fetch(url)
            .then(response => response.json())
            .then(data => {
                showListAllData = data;  // use global variable
                const searchInput = document.getElementById("showListSearch");
                const query = searchInput ? searchInput.value.trim().toLowerCase() : "";
                displayFilteredList(query);
            })
            .catch(err => {
                document.getElementById("listContent").innerHTML = `<p>Error loading ${type.toLowerCase()}s.</p>`;
            });
    }
    // Display filtered list based on query
    function displayFilteredList(query) {
        let filtered = showListAllData;
            if (query) {
                filtered = showListAllData.filter(item => {
                    if (item.resource && item.resource.toLowerCase().includes(query)) return true;
                    if (currentShowListType === "Link" && item.link && item.link.toLowerCase().includes(query)) return true;
                    if (currentShowListType === "Wallet" && item.address && item.address.toLowerCase().includes(query)) return true;
                    return false;
                });
            }
            if (currentShowListType === "Account") renderAccountList(filtered);
            else if (currentShowListType === "Card") renderCardList(filtered);
            else if (currentShowListType === "Link") renderLinkList(filtered);
            else if (currentShowListType === "Wallet") renderWalletList(filtered);
    }
    // These functions take a filtered array and display it
    function renderAccountList(data) {
        const listContent = document.getElementById("listContent");
        if (data.length === 0) {
            listContent.innerHTML = `<p>No accounts found.</p>`;
            return;
        }
        listContent.innerHTML = "";
        data.forEach(account => {
            const row = document.createElement("div");
            row.style.marginBottom = "20px";
            row.innerHTML = `
                <div class="horizontal-group">
                    <label class="form-label">Resource:</label>
                    <input class="form-input" type="text" value="${account.resource}" readonly>
                    <button class="icon-button edit-btn"><img src="/img/Icons/edit_24_White.png" alt="Edit"></button>
                    <button class="icon-button delete-btn"><img src="/img/Icons/delete_24_White.png" alt="Delete"></button>
                </div>
                <div class="horizontal-group">
                    <label class="form-label">Login:</label>
                    <input class="form-input" type="text" value="${account.username}" readonly>
                    <button class="icon-button copy-btn"><img src="/img/Icons/copy_24_White.png" alt="Copy"></button>
                </div>
                <div class="horizontal-group">
                    <label class="form-label">Password:</label>
                    <input class="form-input" type="password" value="${account.password}" readonly>
                    <button class="icon-button toggle-btn"><img src="/img/Icons/visibility_24_White.png" alt="Show"></button>
                    <button class="icon-button copy-btn"><img src="/img/Icons/copy_24_White.png" alt="Copy"></button>
                </div>

                <hr style="margin-top: 20px;">
            `;

            listContent.appendChild(row);

            // Add toggle password visibility
            const toggleBtn = row.querySelector(".toggle-btn");
            const passwordInput = row.querySelector('input[type="password"]');
            toggleBtn.addEventListener("click", () => {
                withDoubleConfirmation(() => {
                    passwordInput.type = passwordInput.type === "password" ? "text" : "password";
                });
            });
            // Add handle copy buttons
            row.querySelectorAll(".copy-btn").forEach(copyBtn => {
                copyBtn.addEventListener("click", () => {
                    withDoubleConfirmation(() => {
                        const input = copyBtn.parentElement.querySelector("input");
                        navigator.clipboard.writeText(input.value).then(() => alert("Copied!"));
                    });
                });
            });
            // Add delete button
            const deleteBtn = row.querySelector(".delete-btn");
            deleteBtn.addEventListener("click", () => {
                withDoubleConfirmation(() => {
                    if(confirm(`Are you sure you want to delete account on resource "${account.resource}"?`)) {
                        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

                        fetch(`/account/delete-account/${account.id}`, {
                            method: "DELETE",
                            headers: {
                                [csrfHeader]: csrfToken
                            }
                        })
                        .then(response => response.text())
                        .then(data => {
                            if(data === "OK") {
                                row.remove();
                                alert("Account deleted.");
                            } else {
                                alert("Failed to delete: " + data);
                            }
                        });
                    }
                });
            });
            // Add edit button
            const editBtn = row.querySelector(".edit-btn");
            editBtn.addEventListener("click", () => {
                withDoubleConfirmation(() => {
                    showAccountDialog(account);
                });
            });
        });
    }
    function renderCardList(data) {
        const listContent = document.getElementById("listContent");
        if (data.length === 0) {
            listContent.innerHTML = "<p>No cards found.</p>";
            return;
        }

        listContent.innerHTML = "";
        data.forEach(card => {
            const row = document.createElement("div");
            row.style.marginBottom = "20px";
            row.innerHTML = `
                <div class="horizontal-group">
                    <label class="form-label">Resource:</label>
                    <input class="form-input" type="text" value="${card.resource}" readonly>
                    <button class="icon-button edit-btn"><img src="/img/Icons/edit_24_White.png" alt="Edit"></button>
                    <button class="icon-button delete-btn"><img src="/img/Icons/delete_24_White.png" alt="Delete"></button>
                </div>
                <div class="horizontal-group">
                    <label class="form-label">Number:</label>
                    <input class="form-input" type="text" value="${card.cardNumber}" readonly>
                    <button class="icon-button copy-btn"><img src="/img/Icons/copy_24_White.png" alt="Copy"></button>
                </div>
                <div class="horizontal-group">
                    <label class="form-label">Date:</label>
                    <input class="form-input" type="text" value="${card.expiryDate}" readonly>
                </div>
                <div class="horizontal-group">
                    <label class="form-label">Owner:</label>
                    <input class="form-input" type="text" value="${card.ownerName}" readonly>
                    <button class="icon-button copy-btn"><img src="/img/Icons/copy_24_White.png" alt="Copy"></button>
                </div>
                <div class="horizontal-group">
                    <label class="form-label">CVV:</label>
                    <input class="form-input" type="password" value="${card.cvv}" readonly>
                    <button class="icon-button toggle-btn"><img src="/img/Icons/visibility_24_White.png" alt="Show"></button>
                </div>
                <div class="horizontal-group">
                    <label class="form-label">Pincode:</label>
                    <input class="form-input" type="password" value="${card.cardPin}" readonly>
                    <button class="icon-button toggle-btn"><img src="/img/Icons/visibility_24_White.png" alt="Show"></button>
                </div>
                <div class="horizontal-group">
                    <label class="form-label">Network:</label>
                    <input class="form-input" type="text" value="${card.cardNetwork}" readonly>
                </div>
                <div class="horizontal-group">
                    <label class="form-label">Type:</label>
                    <input class="form-input" type="text" value="${card.cardType}" readonly>
                </div>

                <hr style="margin-top: 20px;">
            `;
            listContent.appendChild(row);
            // Copy buttons
            row.querySelectorAll(".copy-btn").forEach(copyBtn => {
                copyBtn.addEventListener("click", () => {
                    withDoubleConfirmation(() => {
                        const input = copyBtn.parentElement.querySelector("input");
                        navigator.clipboard.writeText(input.value).then(() => alert("Copied!"));
                    });
                });
            });
            // Toggle password visibility ( CVV & PIN )
            const toggleBtns = row.querySelectorAll(".toggle-btn");
            const passwordInputs = row.querySelectorAll('input[type="password"]');
            toggleBtns.forEach((toggleBtn, idx) => {
                toggleBtn.addEventListener("click", () => {
                    withDoubleConfirmation(() => {
                        const input = passwordInputs[idx];
                        input.type = input.type === "password" ? "text" : "password";
                    });
                });
            });
            // Delete card
            const deleteBtn = row.querySelector(".delete-btn");
            deleteBtn.addEventListener("click", () => {
                withDoubleConfirmation(() => {
                    if(confirm(`Are you sure you want to delete card for resource "${card.resource}"?`)) {
                        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                        fetch(`/account/delete-card/${card.id}`, {
                            method: "DELETE",
                            headers: { [csrfHeader]: csrfToken }
                        })
                        .then(response => response.text())
                        .then(data => {
                            if(data === "OK") {
                                row.remove();
                                alert("Card deleted.");
                            } else {
                                alert("Failed to delete: " + data);
                            }
                        });
                    }
                });
            });
            // Edit card
            const editBtn = row.querySelector(".edit-btn");
            editBtn.addEventListener("click", () => {
                withDoubleConfirmation(() => {
                    showCardDialog(card);
                });
            });
        });
    }
    function renderLinkList(data) {
        const listContent = document.getElementById("listContent");
        if (data.length === 0) {
            listContent.innerHTML = `<p>No links found.</p>`;
            return;
        }
        listContent.innerHTML = "";
        data.forEach(link => {
            const row = document.createElement("div");
            row.style.marginBottom = "20px";
            row.innerHTML = `
                <div class="horizontal-group">
                    <label class="form-label">Resource:</label>
                    <input class="form-input" type="text" value="${link.resource}" readonly>
                    <button class="icon-button edit-btn"><img src="/img/Icons/edit_24_White.png" alt="Edit"></button>
                    <button class="icon-button delete-btn"><img src="/img/Icons/delete_24_White.png" alt="Delete"></button>
                </div>
                <div class="horizontal-group">
                    <label class="form-label">Link:</label>
                    <input class="form-input" type="text" value="${link.link}" readonly>
                    <button class="icon-button copy-btn"><img src="/img/Icons/copy_24_White.png" alt="Copy"></button>
                </div>

                <hr style="margin-top: 20px;">
            `;
            listContent.appendChild(row);
            // Copy button
            row.querySelectorAll(".copy-btn").forEach(copyBtn => {
                copyBtn.addEventListener("click", () => {
                    withDoubleConfirmation(() => {
                        const input = copyBtn.parentElement.querySelector("input");
                        navigator.clipboard.writeText(input.value).then(() => alert("Copied!"));
                    });
                });
            });
            // Delete link
            const deleteBtn = row.querySelector(".delete-btn");
            deleteBtn.addEventListener("click", () => {
                withDoubleConfirmation(() => {
                    if(confirm(`Are you sure you want to delete link for resource "${link.resource}"?`)) {
                        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                        fetch(`/account/delete-link/${link.id}`, {
                            method: "DELETE",
                            headers: { [csrfHeader]: csrfToken }
                        })
                        .then(response => response.text())
                        .then(data => {
                            if(data === "OK") {
                                row.remove();
                                alert("Link deleted.");
                            } else {
                                alert("Failed to delete: " + data);
                            }
                        });
                    }
                });
            });
            // Edit link
            const editBtn = row.querySelector(".edit-btn");
            editBtn.addEventListener("click", () => {
                withDoubleConfirmation(() => {
                    showLinkDialog(link);
                });
            });
        });
    }
    function renderWalletList(data) {
        const listContent = document.getElementById("listContent");
        if (data.length === 0) {
            listContent.innerHTML = `<p>No wallets found.</p>`;
            return;
        }
        listContent.innerHTML = "";
        data.forEach(wallet => {
            const row = document.createElement("div");
            row.style.marginBottom = "20px";
            row.innerHTML = `
                <div class="horizontal-group">
                    <label class="form-label">Resource:</label>
                    <input class="form-input" type="text" value="${wallet.resource}" readonly>
                    <button class="icon-button edit-btn"><img src="/img/Icons/edit_24_White.png" alt="Edit"></button>
                    <button class="icon-button delete-btn"><img src="/img/Icons/delete_24_White.png" alt="Delete"></button>
                </div>
                    <label class="form-label">Key words:</label>
                    <textarea class="form-textarea" readonly>${wallet.keyWords}</textarea>
                <div class="horizontal-group">
                    <label class="form-label">Address:</label>
                    <input class="form-input" type="text" value="${wallet.address}" readonly>
                    <button class="icon-button copy-btn"><img src="/img/Icons/copy_24_White.png" alt="Copy"></button>
                </div>
                <div class="horizontal-group">
                    <label class="form-label">Password:</label>
                    <input class="form-input" type="password" value="${wallet.password}" readonly>
                    <button class="icon-button toggle-btn"><img src="/img/Icons/visibility_24_White.png" alt="Show"></button>
                    <button class="icon-button copy-btn"><img src="/img/Icons/copy_24_White.png" alt="Copy"></button>
                </div>

                <hr style="margin-top: 20px;">
            `;
            listContent.appendChild(row);
            // Toggle password visibility ( password )
            const toggleBtn = row.querySelector(".toggle-btn");
            const passwordInput = row.querySelector('input[type="password"]');
            toggleBtn.addEventListener("click", () => {
                withDoubleConfirmation(() => {
                    passwordInput.type = passwordInput.type === "password" ? "text" : "password";
                });
            });
            // Copy button
            row.querySelectorAll(".copy-btn").forEach(copyBtn => {
                copyBtn.addEventListener("click", () => {
                    withDoubleConfirmation(() => {
                        const input = copyBtn.parentElement.querySelector("input");
                        navigator.clipboard.writeText(input.value).then(() => alert("Copied!"));
                    });
                });
            });
            // Delete button
            const deleteBtn = row.querySelector(".delete-btn");
            deleteBtn.addEventListener("click", () => {
                withDoubleConfirmation(() => {
                    if(confirm(`Are you sure you want to delete wallet for resource "${wallet.resource}"?`)) {
                        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                        fetch(`/account/delete-wallet/${wallet.id}`, {
                            method: "DELETE",
                            headers: { [csrfHeader]: csrfToken }
                        })
                        .then(response => response.text())
                        .then(data => {
                            if(data === "OK") {
                                row.remove();
                                alert("Wallet deleted");
                            } else {
                                alert("Failed to delete: " + data);
                            }
                        });
                    }
                });
            });
            // Edit button
            const editBtn = row.querySelector(".edit-btn");
            editBtn.addEventListener("click", () => {
                withDoubleConfirmation(() => {
                    showWalletDialog(wallet);
                });
            });
        });
    }
    function refreshShowList() {
        fetchAndDisplay(currentShowListType);
    }
    // Preferences GUI
    function renderPreferencesUI() {
        middleContent.innerHTML = `
            <h2>Preferences</h2>
            <div class="prefs-container">
                <div class="prefs-left">
                    <button class="prefs-tab" data-tab="account">Account info</button>
                    <button class="prefs-tab" data-tab="security">Security</button>
                    <button class="prefs-tab" data-tab="theme">Theme</button>
                </div>
                <div class="prefs-right" id="prefsContent">
                    <!-- Dynamic content here -->
                </div>
            </div>
        `;

        const prefsTabs = document.querySelectorAll(".prefs-tab");
        const prefsContent = document.getElementById("prefsContent");

        prefsTabs.forEach(tab => {
            tab.addEventListener("click", () => {
                withDoubleConfirmation(() => {
                    const tabType = tab.getAttribute("data-tab");
                    if (tabType === "account") {
                        // Fetch user info from backend
                        fetch("/account/me")
                            .then(res => res.json())
                            .then(user => {
                                renderAccountInfo(user);
                            });
                            function renderAccountInfo(user) {
                                prefsContent.innerHTML = `
                                    <div class="horizontal-group">
                                        <label class="form-label">Email:</label>
                                        <input class="form-input" id="userEmail" type="email" value="${user.email}" readonly>
                                        <button class="form-button" id="editAccountBtn">Edit:</button>
                                    </div>
                                    <div class="horizontal-group">
                                        <label class="form-label">Username:</label>
                                        <input class="form-input" id="userUsername" type="text" value="${user.username}" readonly>
                                    </div>
                                    <div class="horizontal-group">
                                        <label class="form-label">Password:</label>
                                        <input class="form-input" id="userPassword" type="password" value="${user.password}" readonly>
                                        <button class="form-button" id="showPasswordBtn">Show</button>
                                    </div>
                                    <div class="horizontal-group">
                                        <label class="form-label">Additional password:</label>
                                        <input class="form-input" id="userAdditionalPassword" type="password" value="${user.additionalPassword}" readonly>
                                        <button class="form-button" id="showAdditionalPassword">Show</button>
                                    </div>
                                `;
                                // show/hide password
                                document.getElementById("showPasswordBtn").addEventListener("click", function () {
                                    const pwInput = document.getElementById("userPassword");
                                    pwInput.type = pwInput.type === "password" ? "text" : "password";
                                    this.textContent = pwInput.type === "password" ? "Show" : "Hide";
                                });
                                document.getElementById("showAdditionalPassword").addEventListener("click", function () {
                                    const pwInput = document.getElementById("userAdditionalPassword");
                                    pwInput.type = pwInput.type === "password" ? "text" : "password";
                                    this.textContent = pwInput.type === "password" ? "Show" : "Hide";
                                });
                                // Edit button logic
                                document.getElementById("editAccountBtn").addEventListener("click", function () {
                                    // Enable fields
                                    document.getElementById("userEmail").readOnly = false;
                                    document.getElementById("userUsername").readOnly = false;
                                    document.getElementById("userPassword").readOnly = false;
                                    document.getElementById("userAdditionalPassword").readOnly = false;
                                    // Replace edit button
                                    this.style.display = "none";
                                    // Add save and cansel buttons in the same row
                                    const emailGroup = this.parentElement;
                                    const saveBtn = document.createElement("button");
                                    saveBtn.className = "form-button";
                                    saveBtn.textContent = "Save";
                                    emailGroup.appendChild(saveBtn);

                                    const cancelBtn = document.createElement("button");
                                    cancelBtn.className = "form-button";
                                    cancelBtn.textContent = "Cancel";
                                    emailGroup.appendChild(cancelBtn);
                                    // Handle cancel
                                    cancelBtn.addEventListener("click", function () {
                                        fetch("/account/me")
                                            .then(res => res.json())
                                            .then(u => renderAccountInfo(u));
                                    });
                                    // Handle save
                                    saveBtn.addEventListener("click", function () {
                                        const newEmail = document.getElementById("userEmail").value.trim();
                                        const newUsername = document.getElementById("userUsername").value.trim();
                                        const newPassword = document.getElementById("userPassword").value.trim();
                                        const newAdditionalPassword = document.getElementById("userAdditionalPassword").value.trim();
                                        // Validation
                                        if (!newEmail || !newUsername) {
                                            alert("Email and username cannot be empty");
                                            return;
                                        }
                                        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                                        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                                        fetch("/account/update-account-info", {
                                            method: "POST",
                                            headers: {
                                                "Content-type": "application/json",
                                                [csrfHeader]: csrfToken
                                            },
                                            body: JSON.stringify({
                                                email: newEmail,
                                                username: newUsername,
                                                password: newPassword,
                                                additionalPassword: newAdditionalPassword
                                            })
                                        })
                                        .then(res => res.text())
                                        .then(result => {
                                            if (result === "OK") {
                                                alert("Account info updated!");
                                                fetch("/account/me")
                                                    .then(res => res.json())
                                                    .then(u => renderAccountInfo(u));
                                            } else if (result === "USERNAME_CHANGED") {
                                                alert("Username changed. Please log in again.");
                                                fetch("/logout", {
                                                    method: "POST",
                                                    headers: {
                                                        [csrfHeader]: csrfToken
                                                    }
                                                }).then(() => {
                                                    window.location.href = "/login";
                                                });
                                            } else {
                                                alert(result);
                                            }
                                        });

                                    });

                                });
                            }
                    } else if (tabType === "security") {
                        prefsContent.innerHTML = `
                            <button class="form-button" id="doubleConfirmBtn">Double confirmation</button>
                            <button class="form-button" id="storeLogsBtn">Store logs</button>
                            <button class="form-button" id="clearLogsBtn">Clear logs</button>
                        `;
                        // Double confirmation checkbox logic
                        document.getElementById("doubleConfirmBtn").addEventListener("click", function() {
                            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                            fetch("/settings/toggle-double-confirmation", {
                                method: "POST",
                                headers: {
                                    [csrfHeader]: csrfToken
                                }
                            })
                            .then(response => response.text())
                            .then(data => {
                                if(data === "OK") {
                                    alert("Double confirmation setting toggled");
                                    fetchDoubleConfirmationSettings();
                                } else {
                                    alert("Error toggling double confirmation");
                                }
                            });
                        });
                        // Store logs checkbox logic
                        document.getElementById("storeLogsBtn").addEventListener("click", function() {
                            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                            fetch("/settings/toggle-store-logs", {
                                method: "POST",
                                headers: {
                                    [csrfHeader]: csrfToken
                                }
                            })
                            .then(response => response.text())
                            .then(data => {
                                if (data === "OK") {
                                    alert("Store logs setting toggled");
                                } else {
                                    alert("Error toggling store logs");
                                }
                            });
                        });
                        // Clear logs button logic
                        document.getElementById("clearLogsBtn").addEventListener("click", function(){
                            if(confirm("Are you sure you want to clear all logs?")) {
                                const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

                                fetch("/account/clear-logs", {
                                    method: "GET",
                                    headers: {
                                        [csrfHeader]: csrfToken
                                    }
                                })
                                .then(response => response.text())
                                .then(data => {
                                    if(data === "OK") {
                                        alert("Logs cleared!");
                                        if(document.getElementById("logsPanel") && !document.getElementById("logsPanel").classList.contains("hidden")) {
                                            loadLogs();
                                        }
                                    } else {
                                        alert("Failed to clear logs: " + data);
                                    }
                                })
                                .catch(error => {
                                    alert("Request failed: " + error);
                                });
                            }
                        });
                    } else if (tabType === "theme") {
                        prefsContent.innerHTML = `
                            <label class="form-label">Click to switch the theme:</label><br>
                            <button class="form-button" id="themeSwitchBtn">Switch Theme</button>
                        `;
                        document.getElementById("themeSwitchBtn").addEventListener("click", toggleTheme);
                    }
                });
            });
        });
    }

    // Optional theme toggler stub
    function toggleTheme() {
        const themeLink = document.getElementById("theme-style");
        const currentHref = themeLink.getAttribute("href");
        const newTheme = currentHref.includes("dark.css") ? "light" : "dark";
        themeLink.setAttribute("href", `/css/${newTheme}.css`);

        // Save to MongoDB
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        fetch("/settings", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ theme: newTheme })
        })
        .then(response => response.text())
        .then(result => {
            if (result === "OK") {
                console.log("Theme saved:", newTheme);
            } else {
                alert("Failed to save theme.");
            }
        });
    }


    const logsToggle = document.getElementById("logsToggle");
    const logsPanel = document.getElementById("logsPanel");

    logsToggle.addEventListener("click", () => {
        logsPanel.classList.toggle("hidden");
        logsToggle.classList.toggle("active");
        if (!logsPanel.classList.contains("hidden")) {
            loadLogs();
        }
    });
    let loadedLogs = [];
    // Simulated log loader (in real app this would fetch from a file)
    function loadLogs() {
        fetch("/account/logs")
            .then(response => response.json())
            .then(data => {
                loadedLogs = data;
                displayLogs(loadedLogs);
            })
            .catch(err => {
                document.getElementById("logText").value = "Failed to load logs.";
            });
    }
    function displayLogs(logArray) {
        document.getElementById("logText").value = logArray.join('\n');
    }
    // Create the variable that get the value of logs searchbar
    const logsSearchInput = document.getElementById("logsSearch");
    // Add listener that check and set the search input
    logsSearchInput.addEventListener("input", function() {
        const query = logsSearchInput.value.trim().toLowerCase();
        if(!query) {
            displayLogs(loadedLogs);
            return;
        }
        // Filter logs that include the query ( case-insensitive )
        const filtered = loadedLogs.filter(log => log.toLowerCase().includes(query));
        displayLogs(filtered);
    });


    // Dialog windows
    // Account dialog
    function showAccountDialog(account) {
        const overlay = document.createElement("div");
        overlay.style.position = "fixed";
        overlay.style.top = "0";
        overlay.style.left = "0";
        overlay.style.width = "100%";
        overlay.style.height = "100%";
        overlay.style.backgroundColor = "rgba(0, 0, 0, 0.7)";
        overlay.style.display = "flex";
        overlay.style.alignItems = "center";
        overlay.style.justifyContent = "center";
        overlay.style.zIndex = "9999";

        const dialog = document.createElement("div");
        dialog.style.backgroundColor = "#222";
        dialog.style.padding = "30px";
        dialog.style.borderRadius = "10px";
        dialog.style.width = "400px";

        dialog.innerHTML = `
            <h3>Edit account</h3>
            <label class="form-label">Resource:</label>
            <input id="editResource" class="form-input" value="${account.resource}"><br>
            <label class="form-label">Login:</label>
            <input id="editUsername" class="form-input" value="${account.username}"><br>
            <label class="form-label">Password:</label>
            <input id="editPassword" class="form-input" type="text" value="${account.password}"><br><br>
            <button id="saveEditBtn" class="form-button">Save</button>
            <button id="cancelEditBtn" class="form-button">Cancel</button>
        `;

        overlay.append(dialog);
        document.body.appendChild(overlay);

        document.getElementById("cancelEditBtn").addEventListener("click", () => {
            overlay.remove();
        });

        document.getElementById("saveEditBtn").addEventListener("click", () => {
            const resource = document.getElementById("editResource").value;
            const username = document.getElementById("editUsername").value;
            const password = document.getElementById("editPassword").value;

            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

            fetch("/account/update-account", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    [csrfHeader]: csrfToken
                },
                body: `id=${account.id}&resource=${encodeURIComponent(resource)}&username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
            })
            .then(response => response.text())
            .then(data => {
                if(data === "OK") {
                    alert("Account updated successfully.");
                    overlay.remove();
                    refreshShowList();
                } else {
                    alert("Error: " + data);
                }
            });
        });
    }
    // Card dialog
    function showCardDialog(card) {
        const overlay = document.createElement("div");
        overlay.style.position = "fixed";
        overlay.style.top = "0";
        overlay.style.left = "0";
        overlay.style.width = "100%";
        overlay.style.height = "100%";
        overlay.style.backgroundColor = "rgba(0, 0, 0, 0.7)";
        overlay.style.display = "flex";
        overlay.style.alignItems = "center";
        overlay.style.justifyContent = "center";
        overlay.style.zIndex = "9999";

        const dialog = document.createElement("div");
        dialog.style.backgroundColor = "#222";
        dialog.style.padding = "30px";
        dialog.style.borderRadius = "10px";
        dialog.style.width = "420px";
        dialog.innerHTML = `
            <h3>Edit Card</h3>
            <label class="form-label">Resource:</label>
            <input id="editResource" class="form-input" value="${card.resource}"><br>
            <label class="form-label">Number:</label>
            <input id="editNumber" class="form-input" value="${card.cardNumber}"><br>
            <label class="form-label">Date:</label>
            <input id="editDate" class="form-input" value="${card.expiryDate}"><br>
            <label class="form-label">CVV:</label>
            <input id="editCvv" class="form-input" type="text" value="${card.cvv}"><br>
            <label class="form-label">Owner:</label>
            <input id="editOwner" class="form-input" value="${card.ownerName}"><br>
            <label class="form-label">PIN:</label>
            <input id="editPin" class="form-input" type="text" value="${card.cardPin}"><br>
            <label class="form-label">Pay Network:</label>
            <input id="editNetwork" class="form-input" value="${card.cardNetwork}"><br>
            <label class="form-label">Card Type:</label>
            <input id="editType" class="form-input" value="${card.cardType}"><br><br>
            <button id="saveEditCardBtn" class="form-button">Save</button>
            <button id="cancelEditCardBtn" class="form-button">Cancel</button>
        `;
        overlay.append(dialog);
        document.body.appendChild(overlay);

        document.getElementById("cancelEditCardBtn").addEventListener("click", () => {
            overlay.remove();
        });

        document.getElementById("saveEditCardBtn").addEventListener("click", () => {
            const resource = document.getElementById("editResource").value;
            const number = document.getElementById("editNumber").value;
            const date = document.getElementById("editDate").value;
            const cvv = document.getElementById("editCvv").value;
            const owner = document.getElementById("editOwner").value;
            const pin = document.getElementById("editPin").value;
            const network = document.getElementById("editNetwork").value;
            const type = document.getElementById("editType").value;

            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

            fetch("/account/update-card", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    [csrfHeader]: csrfToken
                },
                body: `id=${card.id}&resource=${encodeURIComponent(resource)}&cardNumber=${encodeURIComponent(number)}&expiryDate=${encodeURIComponent(date)}&cvv=${encodeURIComponent(cvv)}&ownerName=${encodeURIComponent(owner)}&cardPin=${encodeURIComponent(pin)}&cardNetwork=${encodeURIComponent(network)}&cardType=${encodeURIComponent(type)}`
            })
            .then(response => response.text())
            .then(data => {
                if(data === "OK") {
                    alert("Card updated successfully.");
                    overlay.remove();
                    refreshShowList();
                } else {
                    alert("Error: " + data);
                }
            });
        });
    }
    // Link dialog
    function showLinkDialog(link) {
        const overlay = document.createElement("div");
        overlay.style.position = "fixed";
        overlay.style.top = "0";
        overlay.style.left = "0";
        overlay.style.width = "100%";
        overlay.style.height = "100%";
        overlay.style.backgroundColor = "rgba(0, 0, 0, 0.7)";
        overlay.style.display = "flex";
        overlay.style.alignItems = "center";
        overlay.style.justifyContent = "center";
        overlay.style.zIndex = "9999";

        const dialog = document.createElement("div");
        dialog.style.backgroundColor = "#222";
        dialog.style.padding = "30px";
        dialog.style.borderRadius = "10px";
        dialog.style.width = "400px";

        dialog.innerHTML = `
            <h3>Edit Link</h3>
            <label class="form-label">Resource:</label>
            <input id="editResource" class="form-input" type="text" value="${link.resource}"><br>
            <label class="form-label">Link:</label>
            <input id="editLink" class="form-input" type="text" value="${link.link}"><br><br>
            <button id="saveEditBtn" class="form-button">Save</button>
            <button id="cancelEditBtn" class="form-button">Cancel</button>
        `;

        overlay.append(dialog);
        document.body.appendChild(overlay);

        document.getElementById("cancelEditBtn").addEventListener("click", () => {
            overlay.remove();
        });

        document.getElementById("saveEditBtn").addEventListener("click", () => {
            const resource = document.getElementById("editResource").value;
            const linkURL = document.getElementById("editLink").value;
            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

            fetch("/account/update-link", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    [csrfHeader]: csrfToken
                },
                body: `id=${link.id}&resource=${encodeURIComponent(resource)}&linkURL=${encodeURIComponent(linkURL)}`
            })
            .then(response => response.text())
            .then(data => {
                if(data === "OK") {
                    alert("Link updated successfully.");
                    overlay.remove();
                    refreshShowList();
                } else {
                    alert("Error: " + data);
                }
            });
        });
    }
    // Wallet dialog
    function showWalletDialog(wallet) {
        const overlay = document.createElement("div");
        overlay.style.position = "fixed";
        overlay.style.top = "0";
        overlay.style.left = "0";
        overlay.style.width = "100%";
        overlay.style.height = "100%";
        overlay.style.backgroundColor = "rgba(0, 0, 0, 0.7)";
        overlay.style.display = "flex";
        overlay.style.alignItems = "center";
        overlay.style.justifyContent = "center";
        overlay.style.zIndex = "9999";

        const dialog = document.createElement("div");
        dialog.style.backgroundColor = "#222";
        dialog.style.padding = "30px";
        dialog.style.borderRadius = "10px";
        dialog.style.width = "400px";

        dialog.innerHTML = `
            <h3>Edit Wallet</h3>
            <label class="form-label">Resource:</label>
            <input id="editResource" class="form-input" type="text" value="${wallet.resource}"><br>
            <label class="form-label">Key Words:</label>
            <textarea id="editKeyWords" class="form-textarea">${wallet.keyWords}</textarea><br>
            <label class="form-label">Address:</label>
            <input id="editAddress" class="form-input" type="text" value="${wallet.address}"><br>
            <label class="form-label">Password:</label>
            <input id="editPassword" class="form-input" type="text" value="${wallet.password}"><br><br>
            <button id="saveEditBtn" class="form-button">Save</button>
            <button id="cancelEditBtn" class="form-button">Cancel</button>
        `;

        overlay.append(dialog);
        document.body.appendChild(overlay);

        document.getElementById("cancelEditBtn").addEventListener("click", () => {
            overlay.remove();
        });

        document.getElementById("saveEditBtn").addEventListener("click", () => {
            const resource = document.getElementById("editResource").value;
            const keyWords = document.getElementById("editKeyWords").value;
            const address = document.getElementById("editAddress").value;
            const password = document.getElementById("editPassword").value;
            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

            fetch("/account/update-wallet", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    [csrfHeader]: csrfToken
                },
                body: `id=${wallet.id}&resource=${encodeURIComponent(resource)}&keyWords=${encodeURIComponent(keyWords)}&address=${encodeURIComponent(address)}&password=${encodeURIComponent(password)}`
            })
            .then(response => response.text())
            .then(data => {
                if (data === "OK") {
                    alert("Wallet updated successfully.");
                    overlay.remove();
                    refreshShowList();
                } else {
                    alert("Error: " + data);
                }
            });
        });
    }
    function showAdditionalPasswordDialog(onSuccess, onCancel) {
        // Remove existing dialog if any
        let existing = document.getElementById("additionalPwdDialog");
        if (existing) existing.remove();
        // Create overlay
        const overlay = document.createElement("div");
        overlay.id = "additionalPwdDialog";
        overlay.style.position = "fixed";
        overlay.style.top = "0";
        overlay.style.left = "0";
        overlay.style.width = "100vw";
        overlay.style.height = "100vh";
        overlay.style.background = "rgba(0,0,0,0.6)";
        overlay.style.zIndex = "9999";
        overlay.style.display = "flex";
        overlay.style.justifyContent = "center";
        overlay.style.alignItems = "center";
        // Create dialog
        const dialog = document.createElement("div");
        dialog.style.background = "#222";
        dialog.style.padding = "32px";
        dialog.style.borderRadius = "12px";
        dialog.style.boxShadow = "0 0 20px #000";
        dialog.innerHTML = `
            <h3>Double Confirmation</h3>
            <p>Enter your additional password:</p>
            <input type="password" id="additionalPwdInput" class="form-input" style="margin-bottom:12px;width:90%;" autofocus>
            <br>
            <button class="form-button" id="additionalPwdOK">OK</button>
            <button class="form-button" id="additionalPwdCancel">Cancel</button>
        `;
        overlay.appendChild(dialog);
        document.body.appendChild(overlay);

        document.getElementById("additionalPwdOK").onclick = () => {
            const pwd = document.getElementById("additionalPwdInput").value;
            onSuccess(pwd, () => overlay.remove());
        };
        document.getElementById("additionalPwdCancel").onclick = () => {
            overlay.remove();
            if (onCancel) onCancel();
        };
    }
};


