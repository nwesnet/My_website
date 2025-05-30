package nwes.mywebsite;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CryptoService cryptoService;

    public AccountService(AccountRepository accountRepository, CryptoService cryptoService) {
        this.accountRepository = accountRepository;
        this.cryptoService = cryptoService;
    }
    public void saveAccount(Account account) {
        System.out.println("Saving account: " + account.getResource() + ", date: " + account.getDateAdded());
        // ... encryption and saving logic
        try {
            SecretKey key = cryptoService.getKeyFromString(account.getUser().getUsername() + account.getUser().getPassword());
            account.setResource(cryptoService.encrypt(account.getResource(), key));
            account.setUsername(cryptoService.encrypt(account.getUsername(), key));
            account.setPassword(cryptoService.encrypt(account.getPassword(), key));
            accountRepository.save(account);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: ", e);
        }

    }
    public void saveSyncAccount(Account account) {
        Optional<Account> existing = accountRepository
                .findByUserAndResourceAndUsernameAndPassword(
                        account.getUser(), account.getResource(), account.getUsername(), account.getPassword()
                );
        if (existing.isEmpty()) {
            accountRepository.save(account);
        }
    }
    public void deleteAccount(Long id){
        accountRepository.deleteById(id);
    }

    public List<Account> getAccountsByUser(User user) {
        List<Account> encryptedAccounts = accountRepository.findByUser(user);
        try {
            SecretKey key = cryptoService.getKeyFromString(user.getUsername() + user.getPassword());
            for (Account account : encryptedAccounts) {
                account.setResource(cryptoService.decrypt(account.getResource(), key));
                account.setUsername(cryptoService.decrypt(account.getUsername(), key));
                account.setPassword(cryptoService.decrypt(account.getPassword(), key));
            }
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: ", e);
        }
        return encryptedAccounts;
    }

    public List<Account> getSyncAccountsByUser(User user) {
        return accountRepository.findByUser(user);
    }
    public Optional<Account> find(Long id) {
        return accountRepository.findById(id);
    }
}
