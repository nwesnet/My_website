package nwes.mywebsite;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Optional;

@Service
public class LinkService {
    private final LinkRepository linkRepository;
    private final CryptoService cryptoService;
    public LinkService(LinkRepository linkRepository, CryptoService cryptoService) {
        this.linkRepository = linkRepository;
        this.cryptoService = cryptoService;
    }
    public void saveLink(Link link) {
        try {
            SecretKey key = cryptoService.getKeyFromString(link.getUser().getUsername() + link.getUser().getPassword());
            link.setResource(cryptoService.encrypt(link.getResource(), key));
            link.setLink(cryptoService.encrypt(link.getLink(), key));
            linkRepository.save(link);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: ", e);
        }
    }
    public void deleteLink(String id) {
        linkRepository.deleteById(id);
    }
    public List<Link> getLinksByUser(User user) {
        List<Link> encryptedLinks = linkRepository.findByUser(user);
        try {
            SecretKey key = cryptoService.getKeyFromString(user.getUsername() + user.getPassword());
            for (Link link : encryptedLinks) {
                link.setResource(cryptoService.decrypt(link.getResource(), key));
                link.setLink(cryptoService.decrypt(link.getLink(), key));
            }
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: ", e);
        }
        return encryptedLinks;
    }
    public Optional<Link> find(String id) {
        return linkRepository.findById(id);
    }
}
