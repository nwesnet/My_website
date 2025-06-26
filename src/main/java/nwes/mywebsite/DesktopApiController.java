package nwes.mywebsite;

import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.hierarchicalroles.CycleInRoleHierarchyException;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class DesktopApiController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final AccountService accountService;
    private final CardService cardService;
    private final LinkService linkService;
    private final WalletService walletService;
    private final CryptoService cryptoService;


    public DesktopApiController(
            UserService userService,
            UserRepository userRepository,
            UserSettingsRepository userSettingsRepository,
            AccountService accountService,
            CardService cardService,
            LinkService linkService,
            WalletService walletService,
            CryptoService cryptoService
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.accountService = accountService;
        this.cardService = cardService;
        this.linkService = linkService;
        this.walletService = walletService;
        this.cryptoService = cryptoService;
    }

    @GetMapping("/login")
    public ResponseEntity<?> desktopLogin(Principal principal) {
        //  Get the authenticated username from Spring Security
        String username = principal.getName();

        //  Fetch user from MySQL (users table)
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
        }
        //  Check if the user's data should be reencrypted on pc
        if (user.getRole() != null && user.getRole().startsWith("MIGRATION_PENDING")) {
            Long myId = user.getId();

            Optional<User> tempUserOpt = userRepository.findByRole(myId.toString());
            if (tempUserOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Miration temp user not found");
            }
            try {
                // Save get old user
                User oldUser = tempUserOpt.get();
                // create migration key
                SecretKey key = cryptoService.getKeyFromString(user.getUsername() + user.getPassword());
                // encrypt fields
                String encCurrentUsername = cryptoService.encrypt(user.getUsername(), key);
                String encCurrentPassword = cryptoService.encrypt(user.getPassword(), key);
                String encCurrentAdditionalPassword = cryptoService.encrypt(user.getAdditionalPassword(), key);

                String oldUserStripped = oldUser.getUsername().replace("_temp_username", "");
                String encOldUsername = cryptoService.encrypt(oldUserStripped, key);
                String encOldPassword = cryptoService.encrypt(oldUser.getPassword(), key);
                String encOldAdditionalPassword = cryptoService.encrypt(oldUser.getAdditionalPassword(), key);
                // create the json structure
                Map<String, Object> result = new LinkedHashMap<>();
                Map<String, String> current = Map.of(
                        "username", encCurrentUsername,
                        "password", encCurrentPassword,
                        "additionalPassword", encCurrentAdditionalPassword
                );
                Map<String, String> old = Map.of(
                        "username", encOldUsername,
                        "password", encOldPassword,
                        "additionalPassword", encOldAdditionalPassword
                );
                result.put("migration_needed", true);
                result.put("current", current);
                result.put("old", old);
                // send json file with old and new usernames and passwords on pc to reencryp the local database
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            //  Fetch user settings from MongoDB (user_settings)
            UserSettings settings = userSettingsRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User's settings not found"));
            if (settings == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Settings not found.");
            }
            try {
                SecretKey key = cryptoService.getKeyFromString(user.getUsername() + user.getPassword());
                //  Encrypt all fields (like on the PC)
                String encUsername = cryptoService.encrypt(user.getUsername(), key);
                String encPassword = cryptoService.encrypt(user.getPassword(), key);
                String encPincode = cryptoService.encrypt(user.getAdditionalPassword(), key);

                String encDoubleConf = cryptoService.encrypt(Boolean.toString(settings.isDoubleConfirmation()), key);
                String encStoreLogs = cryptoService.encrypt(Boolean.toString(settings.isStoreLogs()), key);
                String encSync = cryptoService.encrypt("true", key); // always "true"

                //  Create your JSON structure
                Map<String, Object> result = new LinkedHashMap<>();
                Map<String, String> loginInfo = Map.of(
                        "username", encUsername,
                        "password", encPassword,
                        "pincode", encPincode
                );
                Map<String, String> security = Map.of(
                        "double_confirmation", encDoubleConf,
                        "store_logs", encStoreLogs
                );
                result.put("login_info", loginInfo);
                result.put("security", security);
                result.put("theme", settings.getTheme());
                result.put("sync", encSync);

                return ResponseEntity.ok(result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        System.out.println("API REGISTER endpoint hit: " + req.getUsername());
        // Validate email format (simple regex)
        if (req.getEmail() == null || !req.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest().body("Invalid email format.");
        }
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body("Username is required.");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Password is required.");
        }
        if (req.getAdditionalPassword() == null || req.getAdditionalPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Additional password is required.");
        }

        // Check if email or username already exists
        if (userService.emailExists(req.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists.");
        }
        if (userService.usernameExists(req.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists.");
        }

        // Create and save user
        User user = new User();
        user.setEmail(req.getEmail());
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword()); // You may want to hash!
        user.setAdditionalPassword(req.getAdditionalPassword());
        userService.register(user);

        // Send OK (desktop app will know it's fine)
        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/sync-accounts")
    public ResponseEntity<?> syncAccounts(
            @RequestBody List<Account> desktopAccounts,
            Principal principal
    ) {
        // ...rest of code
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));

        for (Account account : desktopAccounts) {
            account.setUser(user);
            accountService.saveSyncAccount(account);
        }

        List<Account> allAccounts = accountService.getSyncAccountsByUser(user);
        for (Account acc : allAccounts) {
            if ("true".equalsIgnoreCase(acc.getDeleted())) {
                accountService.deleteAccount(acc.getId());
            }
        }

        return ResponseEntity.ok(allAccounts);
    }
    // --- SYNC CARDS (for desktop sync) ---
    @PostMapping("/sync-cards")
    public ResponseEntity<?> syncCards(
            @RequestBody List<Card> desktopCards,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));

        for (Card card : desktopCards) {
            card.setUser(user);
            cardService.saveSyncCard(card);
        }

        List<Card> allCards = cardService.getSyncCardsByUser(user);
        for (Card card : allCards) {
//            System.out.println("id = " + card.getId() + "\nresource = " + card.getResource() + "\nnumber = " + card.getCardNetwork() + "\ndate = " + card.getExpiryDate() + "\ncvv = " + card.getCvv() + "\nowner = " + card.getOwnerName() + "\npin = " + card.getCardPin() + "\ntype = " + card.getCardNetwork() + "\ntype = " + card.getCardType() + "\nusername = " + card.getOwnerUsername());
            if ("true".equalsIgnoreCase(card.getDeleted())) {
                cardService.deleteCard(card.getId());
            }
        }
        return ResponseEntity.ok(allCards);
    }
    @PostMapping("/sync-links")
    public ResponseEntity<?> syncLinks(
            @RequestBody List<Link> desktopLinks,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));

        for (Link link : desktopLinks) {
            link.setUser(user);
            linkService.saveSyncLink(link);
        }

        List<Link> allLinks = linkService.getSyncLinksByUser(user);
        for (Link link : allLinks) {
            if ("true".equalsIgnoreCase(link.getDeleted())) {
                linkService.deleteLink(link.getId());
            }
        }

        return ResponseEntity.ok(allLinks);
    }
    @PostMapping("/sync-wallets")
    public ResponseEntity<?> syncWallets(
            @RequestBody List<Wallet> desktopWallets,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));

        for (Wallet wallet : desktopWallets) {
            wallet.setUser(user);
            walletService.saveSyncWallet(wallet);
        }

        List<Wallet> allWallets = walletService.getSyncWalletsByUser(user);
        for (Wallet wallet : allWallets) {
            if ("true".equalsIgnoreCase(wallet.getDeleted())) {
                walletService.deleteWallet(wallet.getId());
            }
        }

        return ResponseEntity.ok(allWallets);
    }

    @PostMapping("/add-account")
    public ResponseEntity<?> addAccount(
            @RequestBody Account account,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        account.setUser(user);

        if (accountService.accountExists(user, account.getResource(), account.getUsername(), account.getPassword())) {
            return ResponseEntity.badRequest().body("Account already exists");
        }
        accountService.saveSyncAccount(account);
        return ResponseEntity.ok("OK");
    }
    @PostMapping("/update-account")
    public ResponseEntity<?> updateAccount(
            @RequestBody Account account,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));

        Account serverAccount = accountService.find(account.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (accountService.accountExistsExceptId(user, account.getResource(), account.getUsername(), account.getPassword(), account.getId())) {
            return ResponseEntity.badRequest().body("Account already exists");
        }

        serverAccount.setResource(account.getResource());
        serverAccount.setUsername(account.getUsername());
        serverAccount.setPassword(account.getPassword());
        serverAccount.setLastModified(LocalDateTime.now());

        accountService.saveSyncAccount(serverAccount);

        return ResponseEntity.ok("OK");
    }
    @PostMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(
            @RequestParam String id,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountService.find(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        accountService.deleteAccount(id);
        return ResponseEntity.ok("OK");
    }
    // --- ADD CARD ---
    @PostMapping("/add-card")
    public ResponseEntity<?> addCard(
            @RequestBody Card card,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        card.setUser(user);

        if (cardService.cardExists(user, card.getResource(), card.getCardNumber(), card.getExpiryDate(), card.getOwnerName(), card.getCvv())) {
            return ResponseEntity.badRequest().body("Card already exists");
        }
        cardService.saveSyncCard(card);
        return ResponseEntity.ok("OK");
    }
    // --- UPDATE CARD ---
    @PostMapping("/update-card")
    public ResponseEntity<?> updateCard(
            @RequestBody Card card,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));

        Card serverCard = cardService.find(card.getId())
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (cardService.cardExistsExceptId(user, card.getResource(), card.getCardNumber(), card.getExpiryDate(), card.getOwnerName(), card.getCvv(), card.getId())) {
            return ResponseEntity.badRequest().body("Card already exists");
        }

        serverCard.setResource(card.getResource());
        serverCard.setCardNumber(card.getCardNumber());
        serverCard.setExpiryDate(card.getExpiryDate());
        serverCard.setCvv(card.getCvv());
        serverCard.setOwnerName(card.getOwnerName());
        serverCard.setCardPin(card.getCardPin());
        serverCard.setCardNetwork(card.getCardNetwork());
        serverCard.setCardType(card.getCardType());
        serverCard.setOwnerUsername(card.getOwnerUsername());
        serverCard.setLastModified(LocalDateTime.now());

        cardService.saveSyncCard(serverCard);

        return ResponseEntity.ok("OK");
    }
    // --- DELETE CARD ---
    @PostMapping("/delete-card")
    public ResponseEntity<?> deleteCard(
            @RequestParam String id,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));

        Card card = cardService.find(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        cardService.deleteCard(id);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/add-link")
    public ResponseEntity<?> addLink(
            @RequestBody Link link,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        link.setUser(user);

        if (linkService.linkExists(user, link.getResource(), link.getLink()))
            return ResponseEntity.badRequest().body("Link already exists");

        linkService.saveSyncLink(link);

        return ResponseEntity.ok("OK");
    }
    @PostMapping("/update-link")
    public ResponseEntity<?> updateLink(
            @RequestBody Link link,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));

        Link serverLink = linkService.find(link.getId())
                .orElseThrow(() -> new RuntimeException("Link not found"));

        if (linkService.linkExistsExceptId(user, link.getResource(), link.getLink(), link.getId()))
            return ResponseEntity.badRequest().body("Link already exists");

        serverLink.setResource(link.getResource());
        serverLink.setLink(link.getLink());
        serverLink.setOwnerUsername(link.getOwnerUsername());
        serverLink.setLastModified(link.getLastModified());

        linkService.saveSyncLink(serverLink);

        return ResponseEntity.ok("OK");
    }
    @PostMapping("/delete-link")
    public ResponseEntity<?> deleteLink(
            @RequestParam String id,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));

        Link link = linkService.find(id)
                .orElseThrow(() -> new RuntimeException("Link not found"));

        if (!link.getUser().getId().equals(user.getId()))
            return ResponseEntity.status(403).body("Unauthorized");

        linkService.deleteLink(id);

        return ResponseEntity.ok("OK");
    }

    @PostMapping("/add-wallet")
    public ResponseEntity<?> addWallet(
            @RequestBody Wallet wallet,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        wallet.setUser(user);

        if (walletService.walletExists(
                user,
                wallet.getResource(),
                wallet.getKeyWords(),
                wallet.getAddress(),
                wallet.getPassword()
        )) {
            return ResponseEntity.badRequest().body("Wallet already exists");
        }

        walletService.saveSyncWallet(wallet);
        return ResponseEntity.ok("OK");
    }
    @PostMapping("/update-wallet")
    public ResponseEntity<?> updateWallet(
            @RequestBody Wallet wallet,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet serverWallet = walletService.find(wallet.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (walletService.walletExistsExceptId(
                user,
                wallet.getResource(),
                wallet.getKeyWords(),
                wallet.getAddress(),
                wallet.getPassword(),
                wallet.getId()
        )) {
            return ResponseEntity.badRequest().body("Wallet already exists");
        }

        serverWallet.setResource(wallet.getResource());
        serverWallet.setKeyWords(wallet.getKeyWords());
        serverWallet.setAddress(wallet.getAddress());
        serverWallet.setPassword(wallet.getPassword());
        serverWallet.setLastModified(LocalDateTime.now());

        walletService.saveSyncWallet(serverWallet);
        return ResponseEntity.ok("OK");
    }
    @PostMapping("/delete-wallet")
    public ResponseEntity<?> deleteWallet(
            @RequestParam String id,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletService.find(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (!wallet.getUser().getId().equals(user.getId()))
            return ResponseEntity.status(403).body("Unauthorized");

        walletService.deleteWallet(id);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/check-username")
    public ResponseEntity<?> checkUsername(
            @RequestParam String username,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok("OK");
    }

    @PostMapping("/sync-reencrypt")
    public ResponseEntity<?> syncReencrypt(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String additionalPassword,
            Principal principal
    ) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));

        userService.reencryptUserDataForDesktop(user, username, password, additionalPassword);

        return ResponseEntity.ok("OK");
    }

    @PostMapping("/finish-migration")
    public ResponseEntity<?> finishMigration(Principal principal) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if ("MIGRATION_PENDING".equals(user.getRole())) {
            Long myId = user.getId();
            Optional<User> tempUserOpt = userRepository.findByRole(myId.toString());
            userRepository.delete(tempUserOpt.get());
            user.setRole("USER");
            userRepository.save(user);
        }
        return ResponseEntity.ok("OK");
    }

}
