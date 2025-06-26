package nwes.mywebsite;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
            wallet.setOwnerUsername(cryptoService.encrypt(wallet.getOwnerUsername(), key));
            walletRepository.save(wallet);
        } catch (Exception e) {
            throw new RuntimeException("Encrypted failed: ", e);
        }

    }
    public void saveSyncWallet(Wallet incoming) {
        Optional<Wallet> existingOpt = walletRepository.findById(incoming.getId());
        if (existingOpt.isEmpty()) {
            walletRepository.save(incoming);
            if ("true".equalsIgnoreCase(incoming.getDeleted()))
                walletRepository.delete(incoming);
        } else {
            Wallet existing = existingOpt.get();
            if (incoming.getLastModified().isAfter(existing.getLastModified())) {
                existing.setResource(incoming.getResource());
                existing.setKeyWords(incoming.getKeyWords());
                existing.setAddress(incoming.getAddress());
                existing.setPassword(incoming.getPassword());
                existing.setOwnerUsername(incoming.getOwnerUsername());
                existing.setDateAdded(incoming.getDateAdded());
                existing.setLastModified(incoming.getLastModified());
                existing.setDeleted(incoming.getDeleted());
                existing.setSync(incoming.getSync());
                walletRepository.save(existing);
                if ("true".equalsIgnoreCase(incoming.getDeleted()))
                    walletRepository.delete(existing);
            }
        }
    }
    public void deleteWallet(String id) {
        walletRepository.deleteById(id);
    }
    public void softDeleteWallet(String id, User user) {
        Optional<Wallet> opt = walletRepository.findById(id);
        if (opt.isEmpty())
            throw new RuntimeException("Wallet not found");
        Wallet wallet = opt.get();
        if (!wallet.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Unauthorized");
        wallet.setDeleted("true");
        wallet.setLastModified(LocalDateTime.now());
        walletRepository.save(wallet);
    }
    public List<Wallet> getWalletsByUser(User user, boolean excludeDeleted, boolean onlySync) {
        List<Wallet> encryptedWallets = walletRepository.findByUser(user);
        List<Wallet> decryptedWallets = new ArrayList<>();
        try {
            SecretKey key = cryptoService.getKeyFromString(user.getUsername() + user.getPassword());
            for (Wallet wallet : encryptedWallets) {
                boolean isDeleted = Boolean.parseBoolean(wallet.getDeleted());
                boolean isSync = Boolean.parseBoolean(wallet.getSync());

                if (excludeDeleted && isDeleted) continue;
                if (onlySync && isSync) continue;

                wallet.setResource(cryptoService.decrypt(wallet.getResource(), key));
                wallet.setKeyWords(cryptoService.decrypt(wallet.getKeyWords(), key));
                wallet.setAddress(cryptoService.decrypt(wallet.getAddress(), key));
                wallet.setPassword(cryptoService.decrypt(wallet.getPassword(), key));
                wallet.setOwnerUsername(cryptoService.decrypt(wallet.getOwnerUsername(), key));
                decryptedWallets.add(wallet);
            }
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: ", e);
        }
        return decryptedWallets;
    }
    public List<Wallet> getSyncWalletsByUser(User user) { return walletRepository.findByUser(user); }
    public Optional<Wallet> find(String id) {
        return walletRepository.findById(id);
    }
    public boolean walletExists(User user, String resource, String keyWords, String address, String password) {
        return walletRepository.findByUserAndResourceAndKeyWordsAndAddressAndPassword(user, resource, keyWords, address, password).isPresent();
    }
    public boolean walletExistsExceptId(User user, String resource, String keyWords, String address, String password, String id) {
        return walletRepository.findByUserAndResourceAndKeyWordsAndAddressAndPasswordAndIdNot(user, resource, keyWords, address, password, id).isPresent();
    }

}
