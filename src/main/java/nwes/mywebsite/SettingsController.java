package nwes.mywebsite;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/settings")
public class SettingsController {
    private final UserSettingsService service;
    public SettingsController(UserSettingsService service) {
        this.service = service;
    }
    @GetMapping
    public UserSettings get(Principal principal) {
        System.out.println("Getting settings for user: " + principal.getName());
        return service.getOrCreate(principal.getName());
    }
    @PostMapping
    public String update(@RequestBody UserSettings partial, Principal principal) {
        String username = principal.getName();
        UserSettings current = service.getOrCreate(username);

        if (partial.getTheme() != null) current.setTheme(partial.getTheme());
        if (partial.isStoreLogs() != current.isStoreLogs()) current.setStoreLogs(partial.isStoreLogs());
        if (partial.isDoubleConfirmation() != current.isDoubleConfirmation()) current.setDoubleConfirmation(partial.isDoubleConfirmation());

        current.setUsername(username);
        service.update(current);
        return "OK";
    }

}
