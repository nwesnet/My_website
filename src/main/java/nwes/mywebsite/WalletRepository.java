package nwes.mywebsite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, String> {
    List<Wallet> findByUser(User user);
    Optional<Wallet> findByUserAndResourceAndKeyWordsAndAddressAndPassword(
            User user, String resource, String keyWords, String address, String password
    );
    Optional<Wallet> findByUserAndResourceAndKeyWordsAndAddressAndPasswordAndIdNot(
            User user, String resource, String keyWords, String address, String password, String id
    );
}
