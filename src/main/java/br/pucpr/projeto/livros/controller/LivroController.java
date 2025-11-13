package br.pucpr.projeto.livros.controller;

import br.pucpr.projeto.livros.dto.LivroRequest;
import br.pucpr.projeto.livros.dto.LivroResponse;
import br.pucpr.projeto.livros.repository.CategoriaRepository;
import br.pucpr.projeto.livros.repository.LivroRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroRepository livros;
    private final CategoriaRepository categorias;

    public LivroController(LivroRepository livros, CategoriaRepository categorias) {
        this.livros = livros; this.categorias = categorias;
    }

    @GetMapping
    public List<LivroResponse> list() {
        return livros.findAll().stream().map(l -> new LivroResponse(
                l.getId(), l.getTitulo(), l.getAutor(), l.getCategoria().getId(), l.getCategoria().getNome(), l.getPreco(), l.getIsbn(), l.getImagemCapaUrl()
        )).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivroResponse> get(@PathVariable Long id) {
        return livros.findById(id)
                .map(l -> ResponseEntity.ok(new LivroResponse(l.getId(), l.getTitulo(), l.getAutor(), l.getCategoria().getId(), l.getCategoria().getNome(), l.getPreco(), l.getIsbn(), l.getImagemCapaUrl())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LivroResponse> create(@Valid @RequestBody LivroRequest request) {
        var categoria = categorias.findById(request.categoriaId()).orElse(null);
        if (categoria == null) return ResponseEntity.unprocessableEntity().build();
        // ISBN único
        if (request.isbn() != null && !request.isbn().isBlank() && livros.existsByIsbn(request.isbn())) {
            return ResponseEntity.status(409).build();
        }
    var livro = new br.pucpr.projeto.livros.model.Livro(
        request.titulo(), request.autor(), categoria, request.preco(), request.isbn()
    );
    if (request.imagemCapaUrl() != null && !request.imagemCapaUrl().isBlank()) {
        livro.setImagemCapaUrl(request.imagemCapaUrl());
    }
    livro = livros.save(livro);
    var resp = new LivroResponse(livro.getId(), livro.getTitulo(), livro.getAutor(), categoria.getId(), categoria.getNome(), livro.getPreco(), livro.getIsbn(), livro.getImagemCapaUrl());
        return ResponseEntity.created(URI.create("/api/livros/" + livro.getId())).body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LivroResponse> update(@PathVariable Long id, @Valid @RequestBody LivroRequest request) {
        var opt = livros.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var livro = opt.get();
        var categoria = categorias.findById(request.categoriaId()).orElse(null);
        if (categoria == null) return ResponseEntity.unprocessableEntity().build();
        livro.setTitulo(request.titulo());
        livro.setAutor(request.autor());
        livro.setCategoria(categoria);
        livro.setPreco(request.preco());
        // valida mudança de ISBN
        if (request.isbn() != null && !request.isbn().isBlank()) {
            var novoIsbn = request.isbn();
            boolean isbnAlterou = (livro.getIsbn() == null) || !livro.getIsbn().equalsIgnoreCase(novoIsbn);
            if (isbnAlterou && livros.existsByIsbn(novoIsbn)) {
                return ResponseEntity.status(409).build();
            }
            livro.setIsbn(novoIsbn);
        } else {
            livro.setIsbn(null);
        }
        if (request.imagemCapaUrl() != null && !request.imagemCapaUrl().isBlank()) {
            livro.setImagemCapaUrl(request.imagemCapaUrl());
        }
        livros.save(livro);
        var resp = new LivroResponse(livro.getId(), livro.getTitulo(), livro.getAutor(), categoria.getId(), categoria.getNome(), livro.getPreco(), livro.getIsbn(), livro.getImagemCapaUrl());
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!livros.existsById(id)) return ResponseEntity.notFound().build();
        livros.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
