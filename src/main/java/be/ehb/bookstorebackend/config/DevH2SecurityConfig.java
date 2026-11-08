package be.ehb.bookstorebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("dev")
public class DevH2SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain h2SecurityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .securityMatcher("/h2-console", "/h2-console/**")

                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()
                )

                .csrf(csrf -> csrf.disable())

                .headers(headers ->
                        headers.frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }
}
