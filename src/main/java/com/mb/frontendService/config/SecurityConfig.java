package com.mb.frontendService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/posts/**", "/auth/**", "/css/**", "/js/**", "/users/*/profile").permitAll()
                .requestMatchers("/posts/create", "/posts/*/edit", "/posts/*/delete").authenticated()
                .requestMatchers("/posts/*/comment", "/posts/*/comments/**").authenticated()
                .requestMatchers("/comments/**").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())
            .addFilterBefore(new SessionAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
