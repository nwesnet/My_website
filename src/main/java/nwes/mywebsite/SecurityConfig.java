package nwes.mywebsite;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.DispatcherType;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .map(u -> {
                    String role = u.getRole();
                    if (isNumeric(role)) {
                        throw new UsernameNotFoundException("Temporary users are not allowed to log in");
                    }
                    if (!("USER".equals(role) || "MIGRATION_PENDING".equals(role))) {
                        throw new UsernameNotFoundException("Role not allowed for login: " + role);
                    }
                    return User.withUsername(u.getUsername())
                            .password(u.getPassword())
                            .roles(u.getRole())
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    private boolean isNumeric(String str) {
        if (str == null) return false;
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


// ...

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomLoginSuccessHandler successHandler) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        // Allow forwards (used internally for Thymeleaf/JSP template rendering)
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        // Explicit public pages (URL patterns for GET requests)
                        .requestMatchers("/", "/register", "/login", "/css/**", "/img/**", "/js/**", "/projects/**", "/patches", "/settings/**").permitAll()
                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .httpBasic(basic -> {})
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );
        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        // NoOpPasswordEncoder returns the password as is
        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    }
}


