package nwes.mywebsite;

import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final CryptoService cryptoService;
    public CardService(CardRepository cardRepository, CryptoService cryptoService) {
        this.cardRepository = cardRepository;
        this.cryptoService = cryptoService;
    }
    // Save a card (with encryption)
    public void saveCard(Card card) {
        try {
            SecretKey key = cryptoService.getKeyFromString(card.getUser().getUsername() + card.getUser().getPassword());
            card.setResource(cryptoService.encrypt(card.getResource(), key));
            card.setCardNumber(cryptoService.encrypt(card.getCardNumber(), key));
            card.setExpiryDate(cryptoService.encrypt(card.getExpiryDate(), key));
            card.setCvv(cryptoService.encrypt(card.getCvv(), key));
            card.setOwnerName(cryptoService.encrypt(card.getOwnerName(), key));
            card.setCardPin(cryptoService.encrypt(card.getCardPin(), key));
            card.setCardNetwork(cryptoService.encrypt(card.getCardNetwork(), key));
            card.setCardType(cryptoService.encrypt(card.getCardType(), key));
            card.setOwnerUsername(cryptoService.encrypt(card.getOwnerUsername(), key));
            cardRepository.save(card);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: ", e);
        }
    }
    // Save a card during sync
    public void saveSyncCard(Card incoming) {
        Optional<Card> existingOpt = cardRepository.findById(incoming.getId());
        if (existingOpt.isEmpty()) {
            cardRepository.save(incoming);
            if ("true".equalsIgnoreCase(incoming.getDeleted())) {
                cardRepository.delete(incoming);
            }
        } else {
            Card existing = existingOpt.get();
            if (incoming.getLastModified().isAfter(existing.getLastModified())) {
                // Update fields
                existing.setResource(incoming.getResource());
                existing.setCardNumber(incoming.getCardNumber());
                existing.setExpiryDate(incoming.getExpiryDate());
                existing.setCvv(incoming.getCvv());
                existing.setOwnerName(incoming.getOwnerName());
                existing.setCardPin(incoming.getCardPin());
                existing.setCardNetwork(incoming.getCardNetwork());
                existing.setCardType(incoming.getCardType());
                existing.setOwnerUsername(incoming.getOwnerUsername());
                existing.setLastModified(incoming.getLastModified());
                existing.setDateAdded(incoming.getDateAdded());
                existing.setDeleted(incoming.getDeleted());
                existing.setSync(incoming.getSync());
                cardRepository.save(existing);

                if ("true".equalsIgnoreCase(incoming.getDeleted())) {
                    cardRepository.delete(existing);
                }
            }
        }
    }
    // Hard delete
    public void deleteCard(String id) {
        cardRepository.deleteById(id);
    }
    // Soft delete (sets deleted = "true" and updates lastModified)
    public void softDeleteCard(String id, User user) {
        Optional<Card> opt = cardRepository.findById(id);
        if (opt.isEmpty()) throw new RuntimeException("Card not found");
        Card card = opt.get();
        if (!card.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Unauthorized");
        card.setDeleted("true");
        card.setLastModified(LocalDateTime.now());
        cardRepository.save(card);
    }
    // Get cards for user (optionally filter out deleted or only sync-enabled)
    public List<Card> getCardsByUser(User user, boolean excludeDeleted, boolean onlySync) {
        List<Card> encryptedCards = cardRepository.findByUser(user);
        List<Card> decryptedCards = new ArrayList<>();
        try {
            SecretKey key = cryptoService.getKeyFromString(user.getUsername() + user.getPassword());
            for (Card card : encryptedCards) {
                boolean isDeleted = Boolean.parseBoolean(card.getDeleted());
                boolean isSync = Boolean.parseBoolean(card.getSync());

                if (excludeDeleted && isDeleted) continue;
                if (onlySync && !isSync) continue;

                card.setResource(cryptoService.decrypt(card.getResource(), key));
                card.setCardNumber(cryptoService.decrypt(card.getCardNumber(), key));
                card.setExpiryDate(cryptoService.decrypt(card.getExpiryDate(), key));
                card.setCvv(cryptoService.decrypt(card.getCvv(), key));
                card.setOwnerName(cryptoService.decrypt(card.getOwnerName(), key));
                card.setCardPin(cryptoService.decrypt(card.getCardPin(), key));
                card.setCardNetwork(cryptoService.decrypt(card.getCardNetwork(), key));
                card.setCardType(cryptoService.decrypt(card.getCardType(), key));
                card.setOwnerUsername(cryptoService.decrypt(card.getOwnerUsername(), key));
                decryptedCards.add(card);
            }
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: ", e);
        }
        return decryptedCards;
    }
    // Get all sync cards for user (for server sync endpoint, do not decrypt)
    public List<Card> getSyncCardsByUser(User user) {
        return cardRepository.findByUser(user);
    }
    public Optional<Card> find(String id) {
        return cardRepository.findById(id);
    }
    // Check if a card exists (encrypted values)
    public boolean cardExists(User user, String resource, String cardNumber, String expiryDate, String ownerName, String cvv) {
        try {
            SecretKey key = cryptoService.getKeyFromString(user.getUsername() + user.getPassword());
            String encResource = cryptoService.encrypt(resource, key);
            String encCardNumber = cryptoService.encrypt(cardNumber, key);
            String encExpiryDate = cryptoService.encrypt(expiryDate, key);
            String encOwnerName = cryptoService.encrypt(ownerName, key);
            String encCvv = cryptoService.encrypt(cvv, key);
            return cardRepository.findByResourceAndCardNumberAndExpiryDateAndOwnerNameAndCvv(
                    encResource, encCardNumber, encExpiryDate, encOwnerName, encCvv
            ).isPresent();
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: ", e);
        }
    }

    // Check if a card exists (except a specific id) - for update check
    public boolean cardExistsExceptId(User user, String resource, String cardNumber, String expiryDate, String ownerName, String cvv, String id) {
        try {
            SecretKey key = cryptoService.getKeyFromString(user.getUsername() + user.getPassword());
            String encResource = cryptoService.encrypt(resource, key);
            String encCardNumber = cryptoService.encrypt(cardNumber, key);
            String encExpiryDate = cryptoService.encrypt(expiryDate, key);
            String encOwnerName = cryptoService.encrypt(ownerName, key);
            String encCvv = cryptoService.encrypt(cvv, key);
            return cardRepository.findByResourceAndCardNumberAndExpiryDateAndOwnerNameAndCvvAndIdNot(
                    encResource, encCardNumber, encExpiryDate, encOwnerName, encCvv, id
            ).isPresent();
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: ", e);
        }
    }
}
