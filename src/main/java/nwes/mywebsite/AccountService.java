package nwes.mywebsite;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    public void saveAccount(Account account) {
        accountRepository.save(account);
    }
    public void deleteAccount(Long id){
        accountRepository.deleteById(id);
    }
    public List<Account> getAccountsByUser(User user) {
        return accountRepository.findByUser(user);
    }
    public Optional<Account> find(Long id) {
        return accountRepository.findById(id);
    }
}
