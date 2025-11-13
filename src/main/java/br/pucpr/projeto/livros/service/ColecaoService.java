package br.pucpr.projeto.livros.service;

import br.pucpr.projeto.auth.repository.UserRepository;
import br.pucpr.projeto.livros.model.ColecaoItem;
import br.pucpr.projeto.livros.model.Livro;
import br.pucpr.projeto.livros.repository.ColecaoItemRepository;
import br.pucpr.projeto.livros.repository.LivroRepository;
import br.pucpr.projeto.livros.repository.CategoriaRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ColecaoService {

    private final UserRepository users;
    private final LivroRepository livros;
    private final ColecaoItemRepository colecao;
    private final ExternalBookLookupService lookup;
    private final PriceService priceService;
    private final CategoriaRepository categorias;
    private static final String USER_NOT_FOUND = "Usuário não encontrado";

    public ColecaoService(UserRepository users, LivroRepository livros, ColecaoItemRepository colecao, ExternalBookLookupService lookup, CategoriaRepository categorias, PriceService priceService) {
        this.users = users; this.livros = livros; this.colecao = colecao; this.lookup = lookup; this.categorias = categorias; this.priceService = priceService;
    }

    @Transactional
    public ColecaoItem adicionarPorIsbn(String isbn, String userEmail) {
    var user = users.findByEmail(userEmail.toLowerCase()).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        var sanitized = isbn == null ? null : isbn.replaceAll("[^0-9Xx]", "").toUpperCase();
        if (sanitized == null || sanitized.isBlank()) throw new IllegalArgumentException("ISBN é obrigatório");

        // Evita duplicar na coleção do usuário
        if (colecao.findByUsuarioAndIsbn(user.getId(), sanitized).isPresent()) {
            throw new IllegalArgumentException("Livro já está na sua coleção");
        }

        // Verifica se já existe Livro no catálogo local
        Livro livro = livros.findByIsbn(sanitized).orElse(null);
        if (livro == null) {
            var info = lookup.lookupByIsbn(sanitized);
            if (info == null || (info.titulo() == null || info.titulo().isBlank())) {
                throw new IllegalArgumentException("Não foi possível encontrar informações para o ISBN informado");
            }
            // Categoria padrão
            var categoria = categorias.findByNomeIgnoreCase("Importados").orElseGet(() -> categorias.save(new br.pucpr.projeto.livros.model.Categoria("Importados")));
            // Preço padrão 0.00
            livro = new Livro(info.titulo(), info.autor() == null ? "" : info.autor(), categoria, BigDecimal.ZERO, sanitized);
            if (info.capa() != null && !info.capa().isBlank()) {
                livro.setImagemCapaUrl(info.capa());
            }
            // Tenta obter preço (se houver integração configurada)
            priceService.getAmazonBrPrice(sanitized).ifPresent(livro::setPreco);
            livro = livros.save(livro);
        }

        var item = new ColecaoItem(user, livro);
        return colecao.save(item);
    }

    public List<ColecaoItem> listar(String userEmail) {
    var user = users.findByEmail(userEmail.toLowerCase()).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        return colecao.findByUsuarioOrderByData(user.getId());
    }

    @Transactional
    public void remover(Long itemId, String userEmail) {
    var user = users.findByEmail(userEmail.toLowerCase()).orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        var it = colecao.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item não encontrado"));
        if (!it.getUsuario().getId().equals(user.getId())) throw new IllegalArgumentException("Você não pode remover este item");
        colecao.delete(it);
    }

    // Método temporário para debug da integração Amazon
    public ResponseEntity<Object> debugAmazonIntegration(String isbn) {
        try {
            var info = lookup.lookupByIsbn(isbn);
            var price = priceService.getAmazonBrPrice(isbn);
            
            return ResponseEntity.ok(Map.of(
                "isbn", isbn,
                "bookInfo", info != null ? Map.of(
                    "titulo", info.titulo(),
                    "autor", info.autor(),
                    "editora", info.editora(),
                    "capa", info.capa(),
                    "descricao", info.descricao(),
                    "anoPublicacao", info.anoPublicacao()
                ) : "null",
                "price", price.orElse(null),
                "priceFormatted", price.map(priceService::formatBR).orElse("N/A")
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage(),
                "isbn", isbn
            ));
        }
    }
}
