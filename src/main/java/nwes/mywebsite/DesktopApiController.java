package nwes.mywebsite;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class DesktopApiController {
    private final UserService userService;
    private final AccountService accountService;
    public DesktopApiController(
            UserService userService,
            AccountService accountService
    ) {
        this.userService = userService;
        this.accountService = accountService;
    }
    @GetMapping("/connectivityCheck")
    public ResponseEntity<String> checkConnectivity() {
        return ResponseEntity.ok("Connection successful");
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
            @RequestParam String username,
            @RequestParam String password,
            @RequestBody List<Account> desktopAccounts
    ) {
        System.out.println("Received sync from desktop for user: " + username);
        System.out.println("Accounts received: " + desktopAccounts.size());
        // ...rest of code
        User user = userService.findByUsername(username)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(() -> new RuntimeException("Unauthorized"));

        for (Account account : desktopAccounts) {
            account.setUser(user);
            accountService.saveSyncAccount(account);
        }

        List<Account> allAccounts = accountService.getSyncAccountsByUser(user);
        return ResponseEntity.ok(allAccounts);
    }
}
