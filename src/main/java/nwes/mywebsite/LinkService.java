package nwes.mywebsite;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
            link.setOwnerUsername(cryptoService.encrypt(link.getOwnerUsername(), key));
            linkRepository.save(link);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: ", e);
        }
    }
    public void saveSyncLink(Link incoming) {
        Optional<Link> existinOpt = linkRepository
                .findById(incoming.getId());
        if (existinOpt.isEmpty()) {
            linkRepository.save(incoming);

            if ("true".equalsIgnoreCase(incoming.getDeleted())) {
                linkRepository.delete(incoming);
            }
        } else {
            Link existing = existinOpt.get();
            if (incoming.getLastModified().isAfter(existing.getLastModified())) {
                existing.setResource(incoming.getResource());
                existing.setLink(incoming.getLink());
                existing.setOwnerUsername(incoming.getOwnerUsername());
                existing.setLastModified(incoming.getLastModified());
                existing.setDateAdded(incoming.getDateAdded());
                existing.setDeleted(incoming.getDeleted());
                existing.setSync(incoming.getSync());
                linkRepository.save(existing);

                if ("true".equalsIgnoreCase(incoming.getDeleted())) {
                    linkRepository.delete(existing);
                }
            }
        }
    }
    public void deleteLink(String id) {
        linkRepository.deleteById(id);
    }
    public void softDeleteLink(String id, User user) {
        Optional<Link> opt = linkRepository.findById(id);
        if (opt.isEmpty()) throw new RuntimeException("Account not found");
        Link link = opt.get();
        if (!link.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Unauthorized");

        link.setDeleted("true");
        link.setLastModified(LocalDateTime.now());
        linkRepository.save(link);
    }
    public List<Link> getLinksByUser(User user, boolean excludeDeleted, boolean onlySync) {
        List<Link> encryptedLinks = linkRepository.findByUser(user);
        List<Link> decryptedLinks = new ArrayList<>();
        try {
            SecretKey key = cryptoService.getKeyFromString(user.getUsername() + user.getPassword());
            for (Link link : encryptedLinks) {
                boolean isDeleted = Boolean.parseBoolean(link.getDeleted());
                boolean isSync = Boolean.parseBoolean(link.getSync());

                if (excludeDeleted && isDeleted) continue;
                if (onlySync && !isSync) continue;

                link.setResource(cryptoService.decrypt(link.getResource(), key));
                link.setLink(cryptoService.decrypt(link.getLink(), key));
                link.setOwnerUsername(cryptoService.decrypt(link.getOwnerUsername(), key));
                decryptedLinks.add(link);
            }
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: ", e);
        }
        return decryptedLinks;
    }
    public List<Link> getSyncLinksByUser(User user) { return linkRepository.findByUser(user); }
    public Optional<Link> find(String id) {
        return linkRepository.findById(id);
    }
    public boolean linkExists(User user, String resource, String link) {
        return linkRepository.findByUserAndResourceAndLink(user, resource, link).isPresent();
    }
    public boolean linkExistsExceptId(User user, String resource, String link, String id) {
        return linkRepository.findByUserAndResourceAndLinkAndIdNot(user, resource, link, id).isPresent();
    }
}
