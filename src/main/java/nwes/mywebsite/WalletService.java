package nwes.mywebsite;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }
    public void saveWallet(Wallet wallet) {
        walletRepository.save(wallet);
    }
    public void deleteWallet(Long id) {
        walletRepository.deleteById(id);
    }
    public List<Wallet> getWalletsByUser(User user) {
        return walletRepository.findByUser(user);
    }
    public Optional<Wallet> find(Long id) {
        return walletRepository.findById(id);
    }

}
