package nwes.mywebsite;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogService {
    private final LogRepository logRepository;
    private final CryptoService cryptoService;

    public LogService(LogRepository logRepository, CryptoService cryptoService) {
        this.logRepository = logRepository;
        this.cryptoService = cryptoService;
    }
    public void log(User user, String message) {
        try {
            SecretKey key = cryptoService.getKeyFromString(user.getUsername() + user.getPassword());
            String encryptedMessage = cryptoService.encrypt(message, key);

            LogEntry entry = new LogEntry();
            entry.setUser(user);
            entry.setLogText(encryptedMessage);
            entry.setLocalDateTime(LocalDateTime.now());
            logRepository.save(entry);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: ", e);
        }
    }
    public List<LogEntry> getLogsForUser(User user) {
        List<LogEntry> encryptedLogs = logRepository.findByUserOrderByLocalDateTimeAsc(user);
        try {
            SecretKey key = cryptoService.getKeyFromString(user.getUsername() + user.getPassword());
            return encryptedLogs.stream().map(entry -> {
                try {
                    String decryptedText = cryptoService.decrypt(entry.getLogText(), key);
                    entry.setLogText(decryptedText);
                    return entry;
                } catch (Exception e) {
                    throw new RuntimeException("Decryption failed (logs): ", e);
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed (logs): ", e);
        }
    }
    public void clearLogs(User user) {
        logRepository.deleteAll(logRepository.findByUserOrderByLocalDateTimeAsc(user));
    }
}
