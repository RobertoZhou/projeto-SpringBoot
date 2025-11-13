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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/seller/anuncios")
public class SellerAnuncioController {
    private final AnuncioService anuncioService;
    private final UserRepository userRepository;

    public SellerAnuncioController(AnuncioService anuncioService, UserRepository userRepository) {
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
    public List<AnuncioResponse> meusAnuncios(Authentication auth){
        User seller = currentUser(auth);
        return anuncioService.listarDoVendedor(seller).stream().map(AnuncioResponse::of).toList();
    }

    @PostMapping
    public ResponseEntity<AnuncioResponse> criar(@Valid @RequestBody AnuncioRequest req, Authentication auth){
        User seller = currentUser(auth);
        Anuncio a = anuncioService.criar(seller, req);
        return ResponseEntity.ok(AnuncioResponse.of(a));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(@PathVariable Long id, @RequestParam("value") String value, Authentication auth){
        User seller = currentUser(auth);
        Anuncio.Status novo = Anuncio.Status.valueOf(value);
        anuncioService.alterarStatus(seller, id, novo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, Authentication auth){
        User seller = currentUser(auth);
        anuncioService.deletar(seller, id);
        return ResponseEntity.noContent().build();
    }
}
