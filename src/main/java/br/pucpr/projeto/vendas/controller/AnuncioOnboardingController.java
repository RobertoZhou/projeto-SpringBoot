package br.pucpr.projeto.vendas.controller;

import br.pucpr.projeto.auth.model.User;
import br.pucpr.projeto.auth.repository.UserRepository;
import br.pucpr.projeto.vendas.dto.AnuncioRequest;
import br.pucpr.projeto.vendas.dto.AnuncioResponse;
import br.pucpr.projeto.vendas.model.Anuncio;
import br.pucpr.projeto.vendas.service.AnuncioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/anuncios")
public class AnuncioOnboardingController {
    private final AnuncioService anuncioService;
    private final UserRepository userRepository;

    public AnuncioOnboardingController(AnuncioService anuncioService, UserRepository userRepository) {
        this.anuncioService = anuncioService;
        this.userRepository = userRepository;
    }

    // Cria anúncio e promove o usuário a SELLER se ainda não for
    @PostMapping("/onboard")
    public ResponseEntity<AnuncioResponse> criarPrimeiro(@Valid @RequestBody AnuncioRequest req, Authentication auth) {
        User u = userRepository.findByEmail(auth.getName()).orElseThrow();
    boolean isSeller = u.getRoles().stream().anyMatch("SELLER"::equalsIgnoreCase);
        if (!isSeller) {
            u.getRoles().add("SELLER");
            userRepository.save(u);
        }
        Anuncio a = anuncioService.criar(u, req);
        return ResponseEntity.ok(AnuncioResponse.of(a));
    }
}
