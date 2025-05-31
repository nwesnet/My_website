package nwes.mywebsite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinkRepository extends JpaRepository<Link, String> {
    List<Link> findByUser(User user);
}
