package br.pucpr.projeto.vendas.controller;

import br.pucpr.projeto.auth.model.User;
import br.pucpr.projeto.auth.repository.UserRepository;
import br.pucpr.projeto.vendas.dto.AnuncioResponse;
import br.pucpr.projeto.vendas.dto.CompraRequest;
import br.pucpr.projeto.vendas.model.Anuncio;
import br.pucpr.projeto.vendas.service.AnuncioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/anuncios")
public class AnuncioPublicController {
    private final AnuncioService anuncioService;
    private final UserRepository userRepository;

    public AnuncioPublicController(AnuncioService anuncioService, UserRepository userRepository) {
        this.anuncioService = anuncioService;
        this.userRepository = userRepository;
    }

    private Optional<Long> tryParseLong(String s){
        if (s == null) return Optional.empty();
        String str = s.trim();
        if (str.isEmpty()) return Optional.empty();
        int start = (str.charAt(0) == '-' || str.charAt(0) == '+') ? 1 : 0;
        if (start >= str.length()) return Optional.empty();
        for (int i = start; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') return Optional.empty();
        }
        return Optional.of(Long.parseLong(str));
    }

    private User currentUser(Authentication auth){
        String name = auth.getName();
        return userRepository.findByEmail(name)
                .or(() -> tryParseLong(name).flatMap(userRepository::findById))
                .orElseThrow();
    }

    @GetMapping
    public List<AnuncioResponse> listar(){
        return anuncioService.listarPublicos().stream().map(AnuncioResponse::of).toList();
    }

    @PostMapping("/{id}/comprar")
    public ResponseEntity<AnuncioResponse> comprar(@PathVariable Long id, @RequestBody CompraRequest req, Authentication auth){
        User buyer = currentUser(auth);
        Anuncio a = anuncioService.comprar(buyer, id, req.quantidade());
        return ResponseEntity.ok(AnuncioResponse.of(a));
    }
}
