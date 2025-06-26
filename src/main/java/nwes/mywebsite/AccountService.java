package nwes.mywebsite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        // ... encryption and saving logic
        try {
            SecretKey key = cryptoService.getKeyFromString(account.getUser().getUsername() + account.getUser().getPassword());
            account.setResource(cryptoService.encrypt(account.getResource(), key));
            account.setUsername(cryptoService.encrypt(account.getUsername(), key));
            account.setPassword(cryptoService.encrypt(account.getPassword(), key));
            account.setOwnerUsername(cryptoService.encrypt(account.getOwnerUsername(), key));
            accountRepository.save(account);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: ", e);
        }

    }
    public void saveSyncAccount(Account incoming) {
        Optional<Account> existingOpt = accountRepository
                .findById(incoming.getId());
        if (existingOpt.isEmpty()) {
            accountRepository.save(incoming);

            if ("true".equalsIgnoreCase(incoming.getDeleted())) {
                accountRepository.delete(incoming);
            }
        } else {
            Account existing = existingOpt.get();
            if (incoming.getLastModified().isAfter(existing.getLastModified())) {
                existing.setUsername(incoming.getUsername());
                existing.setPassword(incoming.getPassword());
                existing.setResource(incoming.getResource());
                existing.setOwnerUsername(incoming.getOwnerUsername());
                existing.setLastModified(incoming.getLastModified());
                existing.setDateAdded(incoming.getDateAdded());
                existing.setDeleted(incoming.getDeleted());
                existing.setSync(incoming.getSync());
                accountRepository.save(existing);

                if ("true".equalsIgnoreCase(incoming.getDeleted())) {
                    accountRepository.delete(existing);
                }
            }
        }
    }
    public void deleteAccount(String id){
        accountRepository.deleteById(id);
    }
    public void softDeleteAccount(String id, User user) {
        Optional<Account> opt = accountRepository.findById(id);
        if (opt.isEmpty()) throw new RuntimeException("Account not found");
        Account account = opt.get();
        if (!account.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Unauthorized");

        account.setDeleted("true");
        account.setLastModified(LocalDateTime.now());
        accountRepository.save(account);
    }

    public List<Account> getAccountsByUser(User user, boolean excludeDeleted, boolean onlySync) {
        List<Account> encryptedAccounts = accountRepository.findByUser(user);
        List<Account> decryptedAccounts = new ArrayList<>();
        try {
            SecretKey key = cryptoService.getKeyFromString(user.getUsername() + user.getPassword());
            for (Account account : encryptedAccounts) {
                boolean isDeleted = Boolean.parseBoolean(account.getDeleted());
                boolean isSync = Boolean.parseBoolean(account.getSync());

                if (excludeDeleted && isDeleted) continue;
                if (onlySync && !isSync) continue;

                account.setResource(cryptoService.decrypt(account.getResource(), key));
                account.setUsername(cryptoService.decrypt(account.getUsername(), key));
                account.setPassword(cryptoService.decrypt(account.getPassword(), key));
                account.setOwnerUsername(cryptoService.decrypt(account.getOwnerUsername(), key));
                decryptedAccounts.add(account);
            }
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: ", e);
        }
        return decryptedAccounts;
    }

    public List<Account> getSyncAccountsByUser(User user) {
        return accountRepository.findByUser(user);
    }
    public Optional<Account> find(String id) {
        return accountRepository.findById(id);
    }
    public boolean accountExists(User user, String resource, String username, String password) {
        return accountRepository.findByUserAndResourceAndUsernameAndPassword(user, resource, username, password).isPresent();
    }

    public boolean accountExistsExceptId(User user, String resource, String username, String password, String id) {
        return accountRepository.findByUserAndResourceAndUsernameAndPasswordAndIdNot(user, resource, username, password, id).isPresent();
    }

}
