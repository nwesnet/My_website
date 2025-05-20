package nwes.mywebsite;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/settings")
public class SettingsController {
    private final UserSettingsService userSettingsService;
    public SettingsController(UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }
    @GetMapping
    public UserSettings get(Principal principal) {
        System.out.println("Getting settings for user: " + principal.getName());
        return userSettingsService.getOrCreate(principal.getName());
    }
    @PostMapping
    public String update(@RequestBody UserSettings partial, Principal principal) {
        String username = principal.getName();
        UserSettings current = userSettingsService.getOrCreate(username);

        if (partial.getTheme() != null) current.setTheme(partial.getTheme());
        if (partial.isStoreLogs() != current.isStoreLogs()) current.setStoreLogs(partial.isStoreLogs());
        if (partial.isDoubleConfirmation() != current.isDoubleConfirmation()) current.setDoubleConfirmation(partial.isDoubleConfirmation());

        current.setUsername(username);
        userSettingsService.update(current);
        return "OK";
    }
    @PostMapping("/toggle-double-confirmation")
    public String toggleDoubleConfirmation(Principal principal) {
        String username = principal.getName();
        UserSettings userSettings = userSettingsService.getOrCreate(username);
        userSettings.setDoubleConfirmation(!userSettings.isDoubleConfirmation());
        userSettingsService.update(userSettings);
        return "OK";
    }
    @PostMapping("/toggle-store-logs")
    public String toggleStoreLogs(Principal principal) {
        String username = principal.getName();
        UserSettings userSettings = userSettingsService.getOrCreate(username);
        userSettings.setStoreLogs(!userSettings.isStoreLogs());
        userSettingsService.update(userSettings);
        return "OK";
    }

}
