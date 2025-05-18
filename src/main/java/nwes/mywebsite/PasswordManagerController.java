package nwes.mywebsite;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/account")
public class PasswordManagerController {

    private final AccountService accountService;
    private final CardService cardService;
    private final LinkService linkService;
    private final WalletService walletService;
    private final LogService logService;
    private final UserRepository userRepository;

    public PasswordManagerController(AccountService accountService,CardService cardService, LinkService linkService, WalletService walletService, LogService logService, UserRepository userRepository) {
        this.accountService = accountService;
        this.cardService = cardService;
        this.linkService = linkService;
        this.walletService = walletService;
        this.logService = logService;
        this.userRepository = userRepository;
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

        Account account = new Account();
        account.setResource(resource);
        account.setUsername(username);
        account.setPassword(password);
        account.setUser(user);
        account.setDateAdded(LocalDateTime.now());

        accountService.saveAccount(account);

        logService.log(user, "[web] Added Account for " + resource);

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

        Card card = new Card();
        card.setResource(resource);
        card.setCardNumber(cardNumber);
        card.setExpiryDate(expiryDate);
        card.setCvv(cvv);
        card.setOwnerName(ownerName);
        card.setCardPin(cardPin);
        card.setCardNetwork(cardNetwork);
        card.setCardType(cardType);
        card.setUser(user);
        card.setDateAdded(LocalDateTime.now());

        cardService.saveCard(card);

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

        Link link = new Link();
        link.setResource(resource);
        link.setLink(linkURL);
        link.setLocalDateTime(LocalDateTime.now());
        link.setUser(user);

        linkService.saveLink(link);

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
        Wallet wallet = new Wallet();
        wallet.setResource(resource);
        wallet.setKeyWords(keyWords);
        wallet.setAddress(address);
        wallet.setPassword(password);
        wallet.setLocalDateTime(LocalDateTime.now());
        wallet.setUser(user);

        walletService.saveWallet(wallet);

        return "OK";
    }
    @GetMapping("/list-accounts")
    @ResponseBody
    public List<Account> getAccounts(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return accountService.getAccountsByUser(user);
    }
    @GetMapping("/list-cards")
    @ResponseBody
    public List<Card> getCards(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cardService.getCardsByUser(user);
    }
    @GetMapping("/list-links")
    @ResponseBody
    public List<Link> getLinks(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return linkService.getLinksByUser(user);
    }
    @GetMapping("/list-wallets")
    @ResponseBody
    public List<Wallet> getWallets(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return walletService.getWalletsByUser(user);
    }
    @PostMapping("/update-account")
    @ResponseBody
    public String updateAccount(
            @RequestParam Long id,
            @RequestParam String resource,
            @RequestParam String username,
            @RequestParam String password,
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        Account account = accountService.find(id).orElseThrow(() -> new RuntimeException("Account not found"));
        if(!account.getUser().getId().equals(user.getId())){
            return "Unauthorized";
        }
        account.setResource(resource);
        account.setUsername(username);
        account.setPassword(password);
        accountService.saveAccount(account);

        logService.log(user, "[web] Edited Account for " + resource);

        return "OK";
    }
    @DeleteMapping("/delete-account/{id}")
    @ResponseBody
    public String deleteAccount(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Account account = accountService.find(id).orElseThrow(() -> new RuntimeException("Account not found"));
        if(!account.getUser().getId().equals(user.getId())) {
            return "Unauthorized";
        }
        accountService.deleteAccount(id);
        logService.log(user, "[web] Deleted Account for " + account.getResource());
        return "OK";
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

