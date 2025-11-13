package br.pucpr.projeto.livros.controller;

import br.pucpr.projeto.livros.dto.CategoriaRequest;
import br.pucpr.projeto.livros.dto.CategoriaResponse;
import br.pucpr.projeto.livros.model.Categoria;
import br.pucpr.projeto.livros.repository.CategoriaRepository;
import br.pucpr.projeto.livros.service.CategorySuggestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaRepository repo;
    private final CategorySuggestionService suggestion;

    public CategoriaController(CategoriaRepository repo, CategorySuggestionService suggestion) {
        this.repo = repo; this.suggestion = suggestion;
    }

    @GetMapping
    public List<CategoriaResponse> list() {
        return repo.findAll().stream().map(c -> new CategoriaResponse(c.getId(), c.getNome())).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(c -> ResponseEntity.ok(new CategoriaResponse(c.getId(), c.getNome())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> create(@Valid @RequestBody CategoriaRequest request) {
        if (repo.existsByNomeIgnoreCase(request.nome())) {
            return ResponseEntity.unprocessableEntity().build();
        }
        var categoria = repo.save(new Categoria(request.nome()));
        var resp = new CategoriaResponse(categoria.getId(), categoria.getNome());
        return ResponseEntity.created(URI.create("/api/categorias/" + categoria.getId())).body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> update(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return repo.findById(id)
                .map(c -> { c.setNome(request.nome()); repo.save(c); return ResponseEntity.ok(new CategoriaResponse(c.getId(), c.getNome())); })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sugerir")
    public ResponseEntity<CategoriaResponse> suggestByIsbn(@RequestParam("isbn") String isbn) {
        var categoria = suggestion.suggestOrCreateByIsbn(isbn);
        return ResponseEntity.ok(new CategoriaResponse(categoria.getId(), categoria.getNome()));
    }
}
