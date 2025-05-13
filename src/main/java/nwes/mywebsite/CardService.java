package nwes.mywebsite;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CardService {
    private final CardRepository cardRepository;
    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }
    public void saveCard(Card card) {
        cardRepository.save(card);
    }
    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }
    public List<Card> getCardsByUser(User user) {
        return cardRepository.findByUser(user);
    }
    public Optional<Card> find(Long id) {
        return cardRepository.findById(id);
    }
}
