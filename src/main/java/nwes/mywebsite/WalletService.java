package nwes.mywebsite;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final CryptoService cryptoService;
    public WalletService(WalletRepository walletRepository, CryptoService cryptoService) {
        this.walletRepository = walletRepository;
        this.cryptoService = cryptoService;
    }
    public void saveWallet(Wallet wallet) {
        try {
            SecretKey key = cryptoService.getKeyFromString(wallet.getUser().getUsername() + wallet.getUser().getPassword());
            wallet.setResource(cryptoService.encrypt(wallet.getResource(), key));
            wallet.setKeyWords(cryptoService.encrypt(wallet.getKeyWords(), key));
            wallet.setAddress(cryptoService.encrypt(wallet.getAddress(), key));
            wallet.setPassword(cryptoService.encrypt(wallet.getPassword(), key));
            walletRepository.save(wallet);
        } catch (Exception e) {
            throw new RuntimeException("Encrypted failed: ", e);
        }

    }
    public void deleteWallet(String id) {
        walletRepository.deleteById(id);
    }
    public List<Wallet> getWalletsByUser(User user) {
        List<Wallet> encryptedWallets = walletRepository.findByUser(user);
        try {
            SecretKey key = cryptoService.getKeyFromString(user.getUsername() + user.getPassword());
            for (Wallet wallet : encryptedWallets) {
                wallet.setResource(cryptoService.decrypt(wallet.getResource(), key));
                wallet.setKeyWords(cryptoService.decrypt(wallet.getKeyWords(), key));
                wallet.setAddress(cryptoService.decrypt(wallet.getAddress(), key));
                wallet.setPassword(cryptoService.decrypt(wallet.getPassword(), key));
            }
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: ", e);
        }
        return encryptedWallets;
    }
    public Optional<Wallet> find(String id) {
        return walletRepository.findById(id);
    }

}
