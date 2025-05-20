// src/main/java/nwes/mywebsite/CustomLoginSuccessHandler.java
package nwes.mywebsite;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final LogService logService;
    private final UserSettingsService userSettingsService;

    public CustomLoginSuccessHandler(UserRepository userRepository,
                                     LogService logService,
                                     UserSettingsService userSettingsService) {
        this.userRepository = userRepository;
        this.logService = logService;
        this.userSettingsService = userSettingsService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            UserSettings userSettings = userSettingsService.getOrCreate(user.getUsername());
            if (userSettings.isStoreLogs()) {
                logService.log(user, "[web] Login");
            }
        }
        response.sendRedirect("/"); // Redirect to home after login
    }
}

