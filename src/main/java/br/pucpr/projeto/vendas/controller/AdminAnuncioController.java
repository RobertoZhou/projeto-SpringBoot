package br.pucpr.projeto.vendas.controller;

import br.pucpr.projeto.vendas.dto.AnuncioResponse;
import br.pucpr.projeto.vendas.repository.AnuncioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/anuncios")
public class AdminAnuncioController {
    private final AnuncioRepository anuncioRepository;

    public AdminAnuncioController(AnuncioRepository anuncioRepository) {
        this.anuncioRepository = anuncioRepository;
    }

    @GetMapping
    public List<AnuncioResponse> listarTodos(){
        return anuncioRepository.findAll().stream().map(AnuncioResponse::of).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id){
        Long anuncioId = java.util.Objects.requireNonNull(id, "id é obrigatório");
        if (!anuncioRepository.existsById(anuncioId)) return ResponseEntity.notFound().build();
        anuncioRepository.deleteById(anuncioId);
        return ResponseEntity.noContent().build();
    }
}
