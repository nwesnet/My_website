package nwes.mywebsite;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository repo;

    public AccountService(AccountRepository repo) {
        this.repo = repo;
    }
    public void saveAccount(Account account) {
        repo.save(account);
    }
    public void deletAccount(Long id){
        repo.deleteById(id);
    }
    public List<Account> getAccountsByUser(User user) {
        return repo.findByUser(user);
    }
    public Optional<Account> find(Long id) {
        return repo.findById(id);
    }
}
