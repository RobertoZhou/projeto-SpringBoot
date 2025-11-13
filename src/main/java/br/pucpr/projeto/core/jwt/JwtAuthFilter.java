package br.pucpr.projeto.core.jwt;

import br.pucpr.projeto.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository users;

    public JwtAuthFilter(JwtTokenProvider tokenProvider, UserRepository users) {
        this.tokenProvider = tokenProvider;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        // Debug simples para inspecionar autenticação por rota
        // System.out.println("[JwtAuthFilter] URI=" + request.getRequestURI() + " hasAuthHeader=" + (header != null));
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                var data = tokenProvider.parse(token);
                // Carrega as roles mais recentes do usuário para evitar problemas com tokens antigos
        Set<String> roles = users.findByEmail(data.email().toLowerCase())
            .map(br.pucpr.projeto.auth.model.User::getRoles)
            .orElse(data.roles());
                var auth = new UsernamePasswordAuthenticationToken(
                        data.email(),
                        null,
                        roles.stream()
                                .map(role -> {
                                    // Adiciona prefixo ROLE_ apenas se não existir
                                    if (role.startsWith("ROLE_")) {
                                        return new SimpleGrantedAuthority(role);
                                    }
                                    return new SimpleGrantedAuthority("ROLE_" + role);
                                })
                                .collect(Collectors.toSet())
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception _) {
                // Token inválido/expirado — segue sem autenticação
            }
        }
        filterChain.doFilter(request, response);
    }
}
