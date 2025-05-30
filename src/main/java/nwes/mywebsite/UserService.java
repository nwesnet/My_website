package nwes.mywebsite;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
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
}

