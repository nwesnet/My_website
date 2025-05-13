package nwes.mywebsite;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }
    public void saveWallet(Wallet wallet) {
        walletRepository.save(wallet);
    }
    public List<Wallet> getWalletsByUser(User user) {
        return walletRepository.findByUser(user);
    }

}
