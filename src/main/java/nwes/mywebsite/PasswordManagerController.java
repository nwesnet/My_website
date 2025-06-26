package nwes.mywebsite;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.management.DescriptorKey;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/account")
public class PasswordManagerController {

    private final AccountService accountService;
    private final CardService cardService;
    private final LinkService linkService;
    private final WalletService walletService;
    private final LogService logService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserSettingsService userSettingsService;

    public PasswordManagerController(AccountService accountService,
                                     CardService cardService,
                                     LinkService linkService,
                                     WalletService walletService,
                                     LogService logService,
                                     UserRepository userRepository,
                                     UserService userService,
                                     UserSettingsService userSettingsService) {
        this.accountService = accountService;
        this.cardService = cardService;
        this.linkService = linkService;
        this.walletService = walletService;
        this.logService = logService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.userSettingsService = userSettingsService;
    }

    @PostMapping("/add-account")
    @ResponseBody
    public String addAccount(
            @RequestParam String resource,
            @RequestParam String username,
            @RequestParam String password,
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (accountService.accountExists(user, resource, username, password)) {
            return "Account already exists";
        }

        Account account = new Account();
        account.setUser(user);
        account.setOwnerUsername(user.getUsername());
        account.setId(UUID.randomUUID().toString());
        account.setResource(resource);
        account.setUsername(username);
        account.setPassword(password);
        account.setDateAdded(LocalDateTime.now());
        account.setLastModified(LocalDateTime.now());
        account.setDeleted("false");
        account.setSync("true");

        accountService.saveAccount(account);

        UserSettings userSettings = userSettingsService.getOrCreate(user.getUsername());
        if (userSettings.isStoreLogs()) {
            logService.log(user, "[web] Added Account for " + resource);
        }

        return "OK";
    }
    @PostMapping("/add-card")
    @ResponseBody
    public String addCard(
            @RequestParam String resource,
            @RequestParam String cardNumber,
            @RequestParam String expiryDate,
            @RequestParam String cvv,
            @RequestParam String ownerName,
            @RequestParam String cardPin,
            @RequestParam String cardNetwork,
            @RequestParam String cardType,
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check for uniqueness
        if (cardService.cardExists(user, resource, cardNumber, expiryDate, ownerName, cvv)) {
            return "Card already exists";
        }

        Card card = new Card();
        card.setUser(user);
        card.setId(UUID.randomUUID().toString());
        card.setResource(resource);
        card.setCardNumber(cardNumber);
        card.setExpiryDate(expiryDate);
        card.setCvv(cvv);
        card.setOwnerName(ownerName);
        card.setCardPin(cardPin);
        card.setCardNetwork(cardNetwork);
        card.setCardType(cardType);
        card.setOwnerUsername(user.getUsername()); // <-- Make sure to set ownerUsername!
        card.setDateAdded(LocalDateTime.now());
        card.setLastModified(LocalDateTime.now());
        card.setDeleted("false");
        card.setSync("true");

        cardService.saveCard(card);

        UserSettings userSettings = userSettingsService.getOrCreate(user.getUsername());
        if (userSettings.isStoreLogs()) {
            logService.log(user, "[web] Added Card for " + resource);
        }

        return "OK";
    }
    @PostMapping("/add-link")
    @ResponseBody
    public String addLink(
            @RequestParam String resource,
            @RequestParam String linkURL,
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (linkService.linkExists(user, resource, linkURL))
            return "Link already exists";

        Link link = new Link();
        link.setUser(user);
        link.setOwnerUsername(principal.getName());
        link.setId(UUID.randomUUID().toString());
        link.setResource(resource);
        link.setLink(linkURL);
        link.setDateAdded(LocalDateTime.now());
        link.setLastModified(LocalDateTime.now());
        link.setDeleted("false");
        link.setSync("true");

        linkService.saveLink(link);

        UserSettings userSettings = userSettingsService.getOrCreate(user.getUsername());
        if (userSettings.isStoreLogs()) {
            logService.log(user, "[web] Added Link for " + resource);
        }

        return "OK";
    }
    @PostMapping("/add-wallet")
    @ResponseBody
    public String addWallet(
            @RequestParam String resource,
            @RequestParam String keyWords,
            @RequestParam String address,
            @RequestParam String password,
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (walletService.walletExists(user, resource, keyWords, address, password))
            return "Wallet already exists";

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setOwnerUsername(principal.getName());
        wallet.setId(UUID.randomUUID().toString());
        wallet.setResource(resource);
        wallet.setKeyWords(keyWords);
        wallet.setAddress(address);
        wallet.setPassword(password);
        wallet.setDateAdded(LocalDateTime.now());
        wallet.setLastModified(LocalDateTime.now());
        wallet.setDeleted("false");
        wallet.setSync("true");

        walletService.saveWallet(wallet);

        UserSettings userSettings = userSettingsService.getOrCreate(user.getUsername());
        if (userSettings.isStoreLogs()) {
            logService.log(user, "[web] Added Wallet for " + resource);
        }

        return "OK";
    }
    @GetMapping("/list-accounts")
    @ResponseBody
    public List<Account> getAccounts(
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return accountService.getAccountsByUser(user, true, false);
    }
    @GetMapping("/list-cards")
    @ResponseBody
    public List<Card> getCards(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cardService.getCardsByUser(user, true, false);
    }
    @GetMapping("/list-links")
    @ResponseBody
    public List<Link> getLinks(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return linkService.getLinksByUser(user, true, false);
    }
    @GetMapping("/list-wallets")
    @ResponseBody
    public List<Wallet> getWallets(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return walletService.getWalletsByUser(user, true, false);
    }
    @PostMapping("/update-account")
    @ResponseBody
    public String updateAccount(
            @RequestParam String id,
            @RequestParam String resource,
            @RequestParam String username,
            @RequestParam String password,
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        Account account = accountService.find(id).orElseThrow(() -> new RuntimeException("Account not found"));
        if (!account.getUser().getId().equals(user.getId())){
            return "Unauthorized";
        }

        if (accountService.accountExistsExceptId(user, resource, username, password, id))
            return "Account already exists";


        account.setResource(resource);
        account.setUsername(username);
        account.setPassword(password);
        account.setOwnerUsername(principal.getName());
        account.setLastModified(LocalDateTime.now());

        accountService.saveAccount(account);

        UserSettings userSettings = userSettingsService.getOrCreate(user.getUsername());
        if (userSettings.isStoreLogs()) {
            logService.log(user, "[web] Edited Account for " + resource);
        }

        return "OK";
    }
    @DeleteMapping("/delete-account/{id}")
    @ResponseBody
    public String deleteAccount(@PathVariable String id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Account account = accountService.find(id).orElseThrow(() -> new RuntimeException("Account not found"));
        if(!account.getUser().getId().equals(user.getId())) {
            return "Unauthorized";
        }
        accountService.softDeleteAccount(id, user);

        UserSettings userSettings = userSettingsService.getOrCreate(user.getUsername());
        if (userSettings.isStoreLogs()) {
            logService.log(user, "[web] Deleted Account for " + account.getResource());
        }

        return "OK";
    }
    @PostMapping("/update-card")
    @ResponseBody
    public String updateCard(
            @RequestParam String id,
            @RequestParam String resource,
            @RequestParam String cardNumber,
            @RequestParam String expiryDate,
            @RequestParam String cvv,
            @RequestParam String ownerName,
            @RequestParam String cardPin,
            @RequestParam String cardNetwork,
            @RequestParam String cardType,
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Card card = cardService.find(id).orElseThrow(() -> new RuntimeException("Card not found"));
        if (!card.getUser().getId().equals(user.getId())) {
            return "Unauthorized";
        }

        // Uniqueness check (except current card)
        if (cardService.cardExistsExceptId(user, resource, cardNumber, expiryDate, ownerName, cvv, id))
            return "Card already exists";

        card.setResource(resource);
        card.setCardNumber(cardNumber);
        card.setExpiryDate(expiryDate);
        card.setCvv(cvv);
        card.setOwnerName(ownerName);
        card.setCardPin(cardPin);
        card.setCardNetwork(cardNetwork);
        card.setCardType(cardType);
        card.setOwnerUsername(user.getUsername());
        card.setLastModified(LocalDateTime.now());

        cardService.saveCard(card);

        UserSettings userSettings = userSettingsService.getOrCreate(user.getUsername());
        if (userSettings.isStoreLogs()) {
            logService.log(user, "[web] Edited Card for " + resource);
        }

        return "OK";
    }

    @DeleteMapping("/delete-card/{id}")
    @ResponseBody
    public String deleteCard(@PathVariable String id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Card card = cardService.find(id).orElseThrow(() -> new RuntimeException("Card not found"));
        if (!card.getUser().getId().equals(user.getId())) {
            return "Unauthorized";
        }
        // Soft delete for consistency, but you can use hard delete if you prefer!
        cardService.softDeleteCard(id, user);

        UserSettings userSettings = userSettingsService.getOrCreate(user.getUsername());
        if (userSettings.isStoreLogs()) {
            logService.log(user, "[web] Deleted Card for " + card.getResource());
        }

        return "OK";
    }
    @PostMapping("/update-link")
    @ResponseBody
    public String updateLink(
            @RequestParam String id,
            @RequestParam String resource,
            @RequestParam String linkURL,
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Link link = linkService.find(id).orElseThrow(() -> new RuntimeException("Link not found"));
        if(!link.getUser().getId().equals(user.getId())) {
            return "Unauthorized";
        }

        if (linkService.linkExistsExceptId(user, resource, linkURL, id))
            return "Link already exists";

        link.setResource(resource);
        link.setLink(linkURL);
        link.setOwnerUsername(principal.getName());
        link.setLastModified(LocalDateTime.now());

        linkService.saveLink(link);

        UserSettings userSettings = userSettingsService.getOrCreate(user.getUsername());
        if (userSettings.isStoreLogs()) {
            logService.log(user, "[web] Edited Link for " + resource);
        }

        return "OK";
    }
    @DeleteMapping("/delete-link/{id}")
    @ResponseBody
    public String deleteLink(@PathVariable String id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Link link = linkService.find(id).orElseThrow(() -> new RuntimeException("Link not found"));
        if(!link.getUser().getId().equals(user.getId())) {
            return "Unauthorized";
        }

        linkService.softDeleteLink(id, user);

        UserSettings userSettings = userSettingsService.getOrCreate(user.getUsername());
        if (userSettings.isStoreLogs()) {
            logService.log(user, "[web] Deleted Link for " + link.getResource());
        }

        return "OK";
    }
    @PostMapping("/update-wallet")
    @ResponseBody
    public String updateWallet(
            @RequestParam String id,
            @RequestParam String resource,
            @RequestParam String keyWords,
            @RequestParam String address,
            @RequestParam String password,
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Wallet wallet = walletService.find(id).orElseThrow(() -> new RuntimeException("Wallet not found"));
        if (!wallet.getUser().getId().equals(user.getId())) {
            return "Unauthorized";
        }

        if (walletService.walletExistsExceptId(user, resource, keyWords, address, password, id))
            return "Wallet already exists";

        wallet.setResource(resource);
        wallet.setKeyWords(keyWords);
        wallet.setAddress(address);
        wallet.setPassword(password);
        wallet.setOwnerUsername(principal.getName());
        wallet.setLastModified(LocalDateTime.now());

        walletService.saveWallet(wallet);

        UserSettings userSettings = userSettingsService.getOrCreate(user.getUsername());
        if (userSettings.isStoreLogs()) {
            logService.log(user, "[web] Edited Wallet for " + resource);
        }

        return "OK";
    }
    @DeleteMapping("/delete-wallet/{id}")
    @ResponseBody
    public String deleteWallet(@PathVariable String id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Wallet wallet = walletService.find(id).orElseThrow(() -> new RuntimeException("Wallet not found"));
        if(!wallet.getUser().getId().equals(user.getId())) {
            return "Unauthorized";
        }
        walletService.softDeleteWallet(id, user);

        UserSettings userSettings = userSettingsService.getOrCreate(user.getUsername());
        if (userSettings.isStoreLogs()) {
            logService.log(user, "[web] Deleted Wallet for " + wallet.getResource());
        }

        return "OK";
    }
    @GetMapping("/me")
    @ResponseBody
    public Map<String, String> getCurrentUser(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, String> userMap = new HashMap<>();
        userMap.put("email", user.getEmail());
        userMap.put("username", user.getUsername());
        userMap.put("password", user.getPassword());
        userMap.put("additionalPassword", user.getAdditionalPassword());
        return userMap;
    }
    @PostMapping("/update-account-info")
    @ResponseBody
    public String updateAccountInfo(
            @RequestBody Map<String, String> map,
            Principal principal
    ) {
        String newEmail = map.get("email");
        String newUsername = map.get("username");
        String newPassword = map.get("password");
        String newAdditionalPassword = map.get("additionalPassword");

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getEmail().equals(newEmail) && userRepository.findByEmail(newEmail).isPresent()) {
            return "Email already taken";
        }
        if (!user.getUsername().equals(newUsername) && userRepository.findByUsername(newUsername).isPresent()) {
            return "Username already taken";
        }
        boolean usernameChanged = !user.getUsername().equals(newUsername);

        userService.reencryptUserDataForWeb(user, newEmail, newUsername, newPassword, newAdditionalPassword);

        return usernameChanged ? "USERNAME_CHANGED" : "OK";
    }
    @PostMapping("/verify-additional-password")
    @ResponseBody
    public Map<String, Boolean> verifyAdditionalPassword(@RequestBody Map<String, String> body, Principal principal) {
        String additionalPassword = body.get("additionalPassword");
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean ok = user.getAdditionalPassword().equals(additionalPassword);
        Map<String, Boolean> result = new HashMap<>();
        result.put("ok", ok);
        return result;
    }
    @GetMapping("/logs")
    @ResponseBody
    public List<String> getLogs(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return logService.getLogsForUser(user).stream()
                .map(entry -> entry.getLogText() + " [" + entry.getLocalDateTime().toString() + "]")
                .toList();
    }
    @GetMapping("/clear-logs")
    @ResponseBody
    public String clearLogs(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        logService.clearLogs(user);
        logService.log(user, "[web] The history was cleared");
        return "OK";
    }


}

