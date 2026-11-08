package com.mission;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }

    @Bean UserDetailsService userDetailsService(
        @Value("${APP_USER:uday}") String username,
        @Value("${APP_PASSWORD:}") String password,
        PasswordEncoder encoder,
        @Value("${spring.profiles.active:dev}") String profile,
        @Value("${JDBC_DATABASE_URL:}") String jdbcUrl,
        @Value("${DB_USER:}") String dbUser,
        @Value("${DB_PASSWORD:}") String dbPassword,
        @Value("${COOKIE_SECURE:false}") boolean secure,
        @Value("${RENDER:false}") boolean render) {
        if(username.isBlank() || username.length()>80 || password.length()<16 || password.length()>72 || password.isBlank()) {
            throw new IllegalStateException("Supply APP_USER and a unique APP_PASSWORD of 16-72 characters.");
        }
        if(profile.contains("prod") || render) {
            if(!jdbcUrl.startsWith("jdbc:postgresql://") || !jdbcUrl.matches(".*[?&]sslmode=(require|verify-full)(&.*)?$")
                    || dbUser.isBlank() || dbPassword.isBlank() || !secure) {
                throw new IllegalStateException("Production requires PostgreSQL with TLS, external DB credentials and secure cookies.");
            }
        }
        return new InMemoryUserDetailsManager(User.withUsername(username).password(encoder.encode(password)).roles("OWNER").build());
    }
    @Bean SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/fonts/**", "/favicon.svg").permitAll()
            .requestMatchers("/", "/index.html", "/login.html", "/style.css", "/app.js", "/enhancements.js", "/favicon.ico", "/login", "/error", "/api/csrf", "/actuator/health", "/actuator/health/readiness", "/actuator/health/liveness").permitAll()
            .anyRequest().hasRole("OWNER"))
            .exceptionHandling(errors -> errors.defaultAuthenticationEntryPointFor((request,response,error)->{
                response.setStatus(401); response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Sign in to continue.\"}");
            }, new AntPathRequestMatcher("/api/**")))
            .formLogin(form -> form.loginPage("/login.html").loginProcessingUrl("/login").defaultSuccessUrl("/",true).permitAll())
            .logout(Customizer.withDefaults())
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .headers(headers -> headers.cacheControl(Customizer.withDefaults())
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'")));
        return http.build();
    }
}
