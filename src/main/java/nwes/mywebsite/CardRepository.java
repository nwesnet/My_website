package nwes.mywebsite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, String> {
    List<Card>findByUser(User user);
    Optional<Card> findByResourceAndCardNumberAndExpiryDateAndOwnerNameAndCvv (
            String resource, String cardNumber, String expiryDate, String cardOwner, String cvv
    );
    Optional<Card> findByResourceAndCardNumberAndExpiryDateAndOwnerNameAndCvvAndIdNot(
            String resource, String cardNumber, String expiryDate, String cardOwner, String cvv, String id
    );
}
