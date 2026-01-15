package com.goalsapp.config;

import com.goalsapp.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;


/**
 * Spring Security configuration for the application.
 *
 * <p>
 * Configures authentication, authorization rules, login/logout behavior,
 * password encoding, and access to the H2 console.
 * </p>
 */
@Configuration
public class SecurityConfig {

    /**
     * Provides a {@link UserDetailsService} backed by the application's
     * {@link com.goalsapp.repository.UserRepository}.
     *
     * @param userRepo repository used to load users by username
     * @return a {@link UserDetailsService} implementation
     *
     * @throws UsernameNotFoundException if the user does not exist
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepo) {
        return username -> userRepo.findByUsername(username)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getPasswordHash())
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Defines the password encoder used for hashing user passwords.
     *
     * @return a {@link PasswordEncoder} based on BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the HTTP security filter chain.
     *
     * <p>
     * Public endpoints include home, login, registration, and the H2 console.
     * All other requests require authentication.
     * </p>
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if a security configuration error occurs
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/h2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/goals/LONG_TERM", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                );

        // H2 console
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2/**"));
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
