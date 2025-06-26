package nwes.mywebsite;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class UserService {

    private final UserRepository repo;
    private final UserSettingsService userSettingsService;
    private final PasswordEncoder encoder;
    private final AccountService accountService;
    private final CardService cardService;
    private final LinkService linkService;
    private final WalletService walletService;
    private final LogService logService;

    public UserService(
            UserRepository repo,
            UserSettingsService userSettingsService,
            PasswordEncoder encoder,
            AccountService accountService,
            CardService cardService,
            LinkService linkService,
            WalletService walletService,
            LogService logService
    ) {
        this.repo = repo;
        this.userSettingsService = userSettingsService;
        this.encoder = encoder;
        this.accountService = accountService;
        this.cardService = cardService;
        this.linkService = linkService;
        this.walletService = walletService;
        this.logService = logService;
    }

    public void register(User user) {
        if (repo.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already taken");
        }
        if (repo.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }
        user.setPassword(user.getPassword());
        user.setAdditionalPassword(user.getAdditionalPassword());
        repo.save(user);
    }
    public boolean emailExists(String email) {
        return repo.findByEmail(email).isPresent();
    }
    public boolean usernameExists(String username) {
        return repo.findByUsername(username).isPresent();
    }
    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    @Transactional
    public boolean reencryptUserDataForWeb(User user, String newEmail, String newUsername, String newPassword, String newAdditionalPassword) {
        String oldUsername = user.getUsername();
        createTemporaryUser(user);

        List<Account> accounts = accountService.getAccountsByUser(user, false, false);
        List<Card> cards = cardService.getCardsByUser(user, false, false);
        List<Link> links = linkService.getLinksByUser(user, false, false);
        List<Wallet> wallets = walletService.getWalletsByUser(user, false, false);
        List<LogEntry> logs = logService.getLogsForUser(user);

        user.setEmail(newEmail);
        user.setUsername(newUsername);
        user.setPassword(newPassword);
        user.setAdditionalPassword(newAdditionalPassword);
        user.setRole("MIGRATION_PENDING");

        for (Account account : accounts) {
            account.setUser(user);
            account.setOwnerUsername(user.getUsername());
            accountService.saveAccount(account);
        }
        for (Card card : cards) {
            card.setUser(user);
            card.setOwnerUsername(user.getUsername());
            cardService.saveCard(card);
        }
        for (Link link : links) {
            link.setUser(user);
            link.setOwnerUsername(user.getUsername());
            linkService.saveLink(link);
        }
        for (Wallet wallet : wallets) {
            wallet.setUser(user);
            wallet.setOwnerUsername(user.getUsername());
            walletService.saveWallet(wallet);
        }
        for (LogEntry log : logs) {
            log.setUser(user);
            logService.saveLog(log);
        }

        repo.save(user);
        userSettingsService.updateUsername(oldUsername, newUsername);

        return true;
    }
    @Transactional
    public boolean reencryptUserDataForDesktop(User user, String newUsername, String newPassword, String newAdditionalPassword) {
        String oldUsername = user.getUsername();

        List<Account> accounts = accountService.getAccountsByUser(user, false, false);
        List<Card> cards = cardService.getCardsByUser(user, false, false);
        List<Link> links = linkService.getLinksByUser(user, false, false);
        List<Wallet> wallets = walletService.getWalletsByUser(user, false, false);
        List<LogEntry> logs = logService.getLogsForUser(user);

        user.setUsername(newUsername);
        user.setPassword(newPassword);
        user.setAdditionalPassword(newAdditionalPassword);
        user.setRole("USER");

        for (Account account : accounts) {
            account.setUser(user);
            account.setOwnerUsername(user.getUsername());
            accountService.saveAccount(account);
        }
        for (Card card : cards) {
            card.setUser(user);
            cardService.saveCard(card);
        }
        for (Link link : links) {
            link.setUser(user);
            linkService.saveLink(link);
        }
        for (Wallet wallet : wallets) {
            wallet.setUser(user);
            walletService.saveWallet(wallet);
        }
        for (LogEntry log : logs) {
            log.setUser(user);
            logService.saveLog(log);
        }

        repo.save(user);

        userSettingsService.updateUsername(oldUsername, newUsername);

        return true;
    }
    @Transactional
    public User createTemporaryUser(User originalUser) {
        User tempUser = new User();
        tempUser.setEmail(originalUser.getEmail() + "_temp_email");
        tempUser.setUsername(originalUser.getUsername() + "_temp_username");
        tempUser.setPassword(originalUser.getPassword());
        tempUser.setAdditionalPassword(originalUser.getAdditionalPassword());
        tempUser.setRole(originalUser.getId().toString());
        return repo.save(tempUser);
    }
}

