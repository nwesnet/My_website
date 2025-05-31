package nwes.mywebsite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, String> {
    List<Wallet> findByUser(User user);
}
