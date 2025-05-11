package nwes.mywebsite;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/account")
public class PasswordManagerController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    public PasswordManagerController(AccountService accountService, UserRepository userRepository) {
        this.accountService = accountService;
        this.userRepository = userRepository;
    }

    @PostMapping("/add")
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
        account.setDateAdded(java.time.LocalDateTime.now());

        accountService.saveAccount(account);

        return "OK";
    }
    @GetMapping("/list")
    @ResponseBody
    public List<Account> getAccounts(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return accountService.getAccountsByUser(user);
    }

}

