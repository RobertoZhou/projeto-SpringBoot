package br.pucpr.projeto.auth.controller;

import br.pucpr.projeto.auth.dto.RegisterRequest;
import br.pucpr.projeto.auth.dto.RegisterResponse;
import br.pucpr.projeto.auth.dto.LoginRequest;
import br.pucpr.projeto.auth.dto.AuthTokenResponse;
import br.pucpr.projeto.auth.dto.UpdateProfileRequest;
import br.pucpr.projeto.auth.dto.UpdateProfileResponse;
import br.pucpr.projeto.auth.service.UserService;
import br.pucpr.projeto.core.jwt.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth, @org.springframework.web.bind.annotation.RequestHeader(value = org.springframework.http.HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (auth == null || !auth.isAuthenticated()) {
            // Fallback: usa SecurityContext explicitamente (alguns resolvers podem não injetar Authentication)
            var ctxAuth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (ctxAuth == null || !ctxAuth.isAuthenticated()) {
                // Como último recurso, tenta parsear o token do header Authorization (se presente)
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    try {
                        var data = jwtTokenProvider.parse(authHeader.substring(7));
                        return ResponseEntity.ok(java.util.Map.of(
                                "email", data.email(),
                                "roles", data.roles().stream().map(r -> r.startsWith("ROLE_") ? r : ("ROLE_" + r)).toList()
                        ));
                    } catch (Exception ignored) { }
                }
                return ResponseEntity.status(401).build();
            }
            auth = ctxAuth;
        }
        return ResponseEntity.ok(java.util.Map.of(
                "email", auth.getName(),
                "roles", auth.getAuthorities().stream().map(a -> a.getAuthority()).toList()
        ));
    }

    @GetMapping("/profile")
    public ResponseEntity<UpdateProfileResponse> getProfile(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).build();
        var profile = userService.getProfileByEmail(auth.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UpdateProfileResponse> updateProfile(Authentication auth,
                                                               @Valid @RequestBody UpdateProfileRequest req) {
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).build();
        var profile = userService.updateProfile(auth.getName(), req);
        return ResponseEntity.ok(profile);
    }
}
