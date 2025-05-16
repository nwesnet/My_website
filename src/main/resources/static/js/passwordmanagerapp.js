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
    function renderShowListUI() {
        middleContent.innerHTML = `
            <div style="margin-bottom: 16px;">
                <button class="form-button tab-btn" data-type="Account">Accounts</button>
                <button class="form-button tab-btn" data-type="Card">Cards</button>
                <button class="form-button tab-btn" data-type="Link">Links</button>
                <button class="form-button tab-btn" data-type="Wallet">Wallets</button>
            </div>
            <div id="listContent"></div>
        `;

        // Default tab
        loadAccountList();

        document.querySelectorAll(".tab-btn").forEach(btn => {
            btn.addEventListener("click", () => {
                const type = btn.getAttribute("data-type");
                if (type === "Account") {
                    loadAccountList();
                } else if (type == "Card") {
                    loadCardList();
                } else if (type == "Link") {
                    loadLinkList();
                } else if (type == "Wallet") {
                    loadWalletList();
                }
                else document.getElementById("listContent").innerHTML = `<p>Coming soon: ${type}</p>`;
            });
        });
    }
    function loadAccountList() {
        fetch("/account/list-accounts")
            .then(response => response.json())
            .then(data => {
                const listContent = document.getElementById("listContent");
                listContent.innerHTML = '';

                if (data.length === 0) {
                    listContent.innerHTML = "<p>No accounts saved.</p>";
                    return;
                }

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
                        passwordInput.type = passwordInput.type === "password" ? "text" : "password";
                    });
                    // Add handle copy buttons
                    row.querySelectorAll(".copy-btn").forEach(copyBtn => {
                        copyBtn.addEventListener("click", () => {
                            const input = copyBtn.parentElement.querySelector("input");
                            navigator.clipboard.writeText(input.value).then(() => alert("Copied!"));
                        });
                    });
                    // Add delete button
                    const deleteBtn = row.querySelector(".delete-btn");
                    deleteBtn.addEventListener("click", () => {
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
                    // Add edit button
                    const editBtn = row.querySelector(".edit-btn");
                    editBtn.addEventListener("click", () => {
                        showDialog(account);
                    });
                });
            })
            .catch(err => {
                console.error("Failed to load accounts:", err);
                document.getElementById("listContent").innerHTML = "<p>Error loading accounts.</p>";
            });
    }
    function loadCardList() {
        fetch("/account/list-cards")
            .then(response => response.json())
            .then(data => {
                const listContent = document.getElementById("listContent");
                listContent.innerHTML = '';

                if (data.length === 0) {
                    listContent.innerHTML = "<p>No cards saved.</p>";
                    return;
                }

                data.forEach(card => {
                    const row = document.createElement("div");
                    row.style.marginBottom = "20px";
                    row.innerHTML = `
                        <div class="horizontal-group">
                            <label class="form-label">Resource:</label>
                            <input class="form-input" type="text" value="${card.resource}" readonly>
                            <button class="icon-button"><img src="/img/Icons/edit_24_White.png" alt="Edit"></button>
                            <button class="icon-button"><img src="/img/Icons/delete_24_White.png" alt="Delete"></button>
                        </div>
                        <div class="horizontal-group">
                            <label class="form-label">Number:</label>
                            <input class="form-input" type="text" value="${card.cardNumber}" readonly>
                            <button class="icon-button"><img src="/img/Icons/copy_24_White.png" alt="Copy"></button>
                        </div>
                        <div class="horizontal-group">
                            <label class="form-label">Date:</label>
                            <input class="form-input" type="text" value="${card.expiryDate}" readonly>
                        </div>
                        <div class="horizontal-group">
                            <label class="form-label">Owner:</label>
                            <input class="form-input" type="text" value="${card.ownerName}" readonly>
                            <button class="icon-button"><img src="/img/Icons/copy_24_White.png" alt="Copy"></button>
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

                    const toggleBtn = row.querySelector(".toggle-btn");
                    const passwordInputs = row.querySelectorAll('input[type="password"]');

                    toggleBtn.addEventListener("click", () => {
                        passwordInputs.forEach(input => {
                            input.type = input.type === "password" ? "text" : "password";
                        });
                    });
                });
            })
            .catch(err => {
                console.error("Failed to load cards:", err);
                document.getElementById("listContent").innerHTML = "<p>Error loading cards.</p>";
            });
    }
    function loadLinkList() {
        fetch("/account/list-links")
            .then(response => response.json())
            .then(data => {
                const listContent = document.getElementById("listContent");
                listContent.innerHTML = '';

                if(data.length == 0) {
                    listContent.innerHTML = "<p>No links saved.</p>";
                    return;
                }

                data.forEach(link => {
                    const row = document.createElement("div");
                    row.style.marginBottom = "20px";
                    row.innerHTML = `
                        <div class="horizontal-group">
                            <label class="form-label">Resource:</label>
                            <input class="form-input" type="text" value="${link.resource}">
                            <button class="icon-button"><img src="/img/Icons/edit_24_White.png" alt="Edit"></button>
                            <button class="icon-button"><img src="/img/Icons/delete_24_White.png" alt="Delete"></button>
                        </div>
                        <div class="horizontal-group">
                            <label class="form-label">Link:</label>
                            <input class="form-input" type="text" value="${link.link}">
                            <button class="icon-button"><img src="/img/Icons/copy_24_White.png" alt="Copy"></button>
                        </div>

                        <hr style="margin-top: 20px;">
                    `;
                    listContent.appendChild(row);


                });
            })
            .catch(err => {
                console.error("Failed to load links:", err);
                document.getElementById("listContent").innerHTML = "<p>Error loading links.</p>";
            });
    }
    function loadWalletList() {
        fetch("/account/list-wallets")
            .then(response => response.json())
            .then(data => {
                const listContent = document.getElementById("listContent");
                listContent.innerHTML = '';

                if(data.length === 0) {
                    listContent.innerHTML = "<p>No wallets saved.</p>";
                    return;
                }

                data.forEach(wallet => {
                    const row = document.createElement("div");
                    row.style.marginBottom = "20px";
                    row.innerHTML = `
                        <div class="horizontal-group">
                            <label class="form-label">Resource:</label>
                            <input class="form-input" type="text" value="${wallet.resource}" readonly>
                            <button class="icon-button"><img src="/img/Icons/edit_24_White.png" alt="Edit"></button>
                            <button class="icon-button"><img src="/img/Icons/delete_24_White.png" alt="Delete"></button>
                        </div>
                        <label class="form-label">Key words:</label>
                        <textarea class="form-textarea" readonly>${wallet.keyWords}</textarea>
                        <div class="horizontal-group">
                            <label class="form-label">Address:</label>
                            <input class="form-input" type="text" value="${wallet.address}" readonly>
                            <button class="icon-button"><img src="/img/Icons/copy_24_White.png" alt="Copy"></button>
                        </div>
                        <div class="horizontal-group">
                            <label class="form-label">Password:</label>
                            <input class="form-input" type="password" value="${wallet.password}" readonly>
                            <button class="icon-button toggle-btn"><img src="/img/Icons/visibility_24_White.png" alt="Show"></button>
                            <button class="icon-button"><img src="/img/Icons/copy_24_White.png" alt="Copy"></button>
                        </div>

                        <hr style="margin-top: 20px;">
                    `;
                    listContent.appendChild(row);

                    const toggleBtn = row.querySelector(".toggle-btn");
                    const passwordInput = row.querySelector('input[type="password"]');
                    toggleBtn.addEventListener("click", () => {
                        passwordInput.type = passwordInput.type === "password" ? "text" : "password";
                    });
                });

            })
            .catch(err => {
                console.error("Failed to load wallets:", err);
                document.getElementById("listContent").innerHTML = "<p>Error loading wallets.</p>";
            });
    }



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
                const tabType = tab.getAttribute("data-tab");
                if (tabType === "account") {
                    prefsContent.innerHTML = `
                        <label class="form-label">Username:</label><input class="form-input" type="text"><br>
                        <label class="form-label">Password:</label><input class="form-input" type="password"><br>
                        <label class="form-label">Additional Password:</label><input class="form-input" type="password"><br>
                    `;
                } else if (tabType === "security") {
                    prefsContent.innerHTML = `
                        <button class="form-button">Double confirmation</button>
                        <button class="form-button">Store logs</button>
                        <button class="form-button">Clear logs</button>
                    `;
                } else if (tabType === "theme") {
                    prefsContent.innerHTML = `
                        <label class="form-label">Click to switch the theme:</label><br>
                        <button class="form-button" id="themeSwitchBtn">Switch Theme</button>
                    `;
                    document.getElementById("themeSwitchBtn").addEventListener("click", toggleTheme);
                }
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

    // Simulated log loader (in real app this would fetch from a file)
    function loadLogs() {
        const sampleLogs = `
    The history was cleared at [2025-04-02 09:07:18]
    Login [2025-04-02 09:42:34]
    Added Account for testgenpsswd
    Added Card for testgenpsswd
    Added Link for testgenpsswd
    [2025-04-02 10:53:28]
    [2025-04-02 11:00:29]
    [2025-04-02 11:01:37]
    [2025-04-02 11:02:31]
    `;
        document.getElementById("logText").value = sampleLogs.trim();
    }

    // Dialog window
    function showDialog(account) {
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
                    loadAccountList();
                } else {
                    alert("Error: " + data);
                }
            });
        });
    }

};


