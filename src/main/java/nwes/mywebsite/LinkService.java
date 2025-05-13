package nwes.mywebsite;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LinkService {
    private final LinkRepository linkRepository;
    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }
    public void saveLink(Link link) {
        linkRepository.save(link);
    }
    public void deleteLink(Long id) {
        linkRepository.deleteById(id);
    }
    public List<Link> getLinksByUser(User user) {
        return linkRepository.findByUser(user);
    }
    public Optional<Link> find(Long id) {
        return linkRepository.findById(id);
    }
}
