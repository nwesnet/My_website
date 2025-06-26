package nwes.mywebsite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, String> {
    List<Link> findByUser(User user);
    Optional<Link> findByUserAndResourceAndLink(User user, String resource, String link);
    Optional<Link> findByUserAndResourceAndLinkAndIdNot(User user, String resource, String link, String id);
}
