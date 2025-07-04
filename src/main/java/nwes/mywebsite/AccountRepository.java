package nwes.mywebsite;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByUser(User user);
    Optional<Account> findByUserAndResourceAndUsernameAndPassword(User user, String resource, String username, String password);
    Optional<Account> findByUserAndResourceAndUsernameAndPasswordAndIdNot(
            User user, String resource, String username, String password, String id
    );

}
