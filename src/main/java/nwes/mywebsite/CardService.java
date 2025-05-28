package nwes.mywebsite;

import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
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
            cardRepository.save(card);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: ", e);
        }
    }
    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }
    public List<Card> getCardsByUser(User user) {
        List<Card> encryptedCards = cardRepository.findByUser(user);
        try {
            SecretKey key = cryptoService.getKeyFromString(user.getUsername() + user.getPassword());
            for (Card card : encryptedCards ) {
                card.setResource(cryptoService.decrypt(card.getResource(), key));
                card.setCardNumber(cryptoService.decrypt(card.getCardNumber(), key));
                card.setExpiryDate(cryptoService.decrypt(card.getExpiryDate(), key));
                card.setCvv(cryptoService.decrypt(card.getCvv(), key));
                card.setOwnerName(cryptoService.decrypt(card.getOwnerName(), key));
                card.setCardPin(cryptoService.decrypt(card.getCardPin(), key));
                card.setCardNetwork(cryptoService.decrypt(card.getCardNetwork(), key));
                card.setCardType(cryptoService.decrypt(card.getCardType(), key));
            }
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: ", e);
        }
        return encryptedCards;
    }
    public Optional<Card> find(Long id) {
        return cardRepository.findById(id);
    }
}
