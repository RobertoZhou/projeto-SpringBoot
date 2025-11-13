package br.pucpr.projeto.core;

import br.pucpr.projeto.core.jwt.JwtAuthFilter;
import br.pucpr.projeto.core.jwt.JwtTokenProvider;
import br.pucpr.projeto.auth.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenProvider jwt, UserRepository users) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/", 
                "/index.html",
                "/home.html",
                "/login.html",
                "/register.html",
                "/admin.html",
                "/test-auth.html",
                "/css/**",
                "/js/**",
                "/img/**",
                "/favicon.ico",
                "/api/auth/register", 
                "/api/auth/login",
                "/api/debug/**",
                "/h2/**"
            ).permitAll()
            // Livros: leitura pública (ou troque para authenticated se quiser)
            .requestMatchers(HttpMethod.GET, "/api/livros/**").permitAll()
            // Livros: escrita apenas ADMIN
            .requestMatchers(HttpMethod.POST, "/api/livros/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/livros/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/livros/**").hasRole("ADMIN")
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(new JwtAuthFilter(jwt, users), UsernamePasswordAuthenticationFilter.class);

        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
