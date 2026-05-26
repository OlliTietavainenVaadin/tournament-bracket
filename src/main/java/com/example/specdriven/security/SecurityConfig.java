package com.example.specdriven.security;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final int NUMBER_OF_PLAYERS = 20;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        SimpleUrlLogoutSuccessHandler logoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        logoutSuccessHandler.setDefaultTargetUrl("/");
        return http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class);
            configurer.logoutSuccessHandler(logoutSuccessHandler);
        }).build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        List<UserDetails> users = new ArrayList<>();
        users.add(User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin")
                .roles("ADMIN")
                .build());
        for (int i = 1; i <= NUMBER_OF_PLAYERS; i++) {
            String username = "player" + i;
            users.add(User.withDefaultPasswordEncoder()
                    .username(username)
                    .password(username)
                    .roles("USER")
                    .build());
        }
        return new InMemoryUserDetailsManager(users);
    }
}
