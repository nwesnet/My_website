package nwes.mywebsite;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogService {
    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }
    public void log(User user, String message) {
        LogEntry entry = new LogEntry();
        entry.setUser(user);
        entry.setLogText(message);
        entry.setLocalDateTime(LocalDateTime.now());
        logRepository.save(entry);
    }
    public List<LogEntry> getLogsForUser(User user) {
        return logRepository.findByUserOrderByLocalDateTimeAsc(user);
    }
    public void clearLogs(User user) {
        logRepository.deleteAll(logRepository.findByUserOrderByLocalDateTimeAsc(user));
    }
}
