package nwes.mywebsite;

import org.springframework.stereotype.Service;

@Service
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    public UserSettingsService(UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
    }
    public UserSettings getOrCreate(String username) {
        UserSettings settings = userSettingsRepository.findByUsername(username).orElse(null);
        if (settings == null) {
            settings = new UserSettings();
            settings.setUsername(username);
            settings.setDoubleConfirmation(false);
            settings.setStoreLogs(true);
            settings.setTheme("dark");
            System.out.println("Creating settings for user: " + username);
        } else {
            System.out.println("Found existing settings for user: " + username);
        }
        return userSettingsRepository.save(settings); // Always save for debug
    }

    public void update(UserSettings settings) {
        userSettingsRepository.save(settings);
    }

    public void updateUsername(String oldUsername, String newUsername) {
        UserSettings settings = userSettingsRepository.findByUsername(oldUsername).orElseThrow(() -> new RuntimeException("User not found"));
        if (settings != null) {
            settings.setUsername(newUsername);
            userSettingsRepository.save(settings);
        }
    }
}
