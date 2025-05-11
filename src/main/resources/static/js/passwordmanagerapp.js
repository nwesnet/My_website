window.onload = function () {
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
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        document.getElementById("saveAccountBtn").addEventListener("click", () => {
            const formInputs = document.querySelectorAll("#formContent .form-input");
            const resource = formInputs[0].value;
            const username = formInputs[1].value;
            const password = formInputs[2].value;

            fetch("/account/add", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    [csrfHeader]: csrfToken // 🔐 CSRF protection
                },
                body: `resource=${encodeURIComponent(resource)}&username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
            })
            .then(response => response.text())
            .then(data => {
                if (data === "OK") {
                    alert("Account saved successfully.");
                    document.getElementById("middleContent").innerHTML = "";
                } else {
                    alert("Error: " + data);
                }
            })
            .catch(error => {
                console.error("Error saving account:", error);
                alert("Failed to save account.");
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
                    toggleTheme();
                }
            });
        });
    }

    // Optional theme toggler stub
    function toggleTheme() {
        alert("Theme toggled! (placeholder)");
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

};


