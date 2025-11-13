package br.pucpr.projeto.livros.service;

import br.pucpr.projeto.livros.model.Categoria;
import br.pucpr.projeto.livros.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.text.Normalizer;
import java.util.*;

@Service
public class CategorySuggestionService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CategorySuggestionService.class);

    private final RestClient googleBooks = RestClient.builder().baseUrl("https://www.googleapis.com/books/v1").build();
    private final RestClient openLibrary = RestClient.builder().baseUrl("https://openlibrary.org").build();
    private final CategoriaRepository categorias;

    public CategorySuggestionService(CategoriaRepository categorias) { this.categorias = categorias; }

    public Categoria suggestOrCreateByIsbn(String rawIsbn) {
        String isbn = sanitizeIsbn(rawIsbn);
        List<String> terms = new ArrayList<>();
        terms.addAll(googleCategories(isbn));
        terms.addAll(openLibrarySubjects(isbn));
        String mapped = mapToInternal(terms);
        final String chosen = mapped != null ? mapped : "Geral";
        return categorias.findByNomeIgnoreCase(chosen).orElseGet(() -> categorias.save(new Categoria(chosen)));
    }

    private List<String> googleCategories(String isbn) {
        List<String> out = new ArrayList<>();
        try {
            var response = googleBooks.get()
                    .uri(uri -> uri.path("/volumes").queryParam("q", "isbn:" + isbn).build())
                    .retrieve()
                    .toEntity(Map.class);
            var body = response.getBody();
            if (body == null || !body.containsKey("items")) return out;
            var itemsObj = body.get("items");
            if (!(itemsObj instanceof List<?> items) || items.isEmpty()) return out;
            var first = items.get(0);
            if (!(first instanceof Map<?,?> firstMap)) return out;
            var volumeInfo = firstMap.get("volumeInfo");
            if (!(volumeInfo instanceof Map<?,?> vi)) return out;
            var cats = vi.get("categories");
            if (cats instanceof List<?> list) for (var c : list) out.add(String.valueOf(c));
        } catch (Exception e) { log.debug("Falha GoogleBooks categorias para {}", isbn, e); }
        return out;
    }

    private List<String> openLibrarySubjects(String isbn) {
        List<String> out = new ArrayList<>();
        try {
            var response = openLibrary.get()
                    .uri(uri -> uri.path("/isbn/" + isbn + ".json").build())
                    .retrieve()
                    .toEntity(Map.class);
            var body = response.getBody();
            if (body == null) return out;
            var subjects = body.get("subjects");
            if (subjects instanceof List<?> list) for (var s : list) out.add(String.valueOf(s));
        } catch (Exception e) { log.debug("Falha OpenLibrary subjects para {}", isbn, e); }
        return out;
    }

    private String sanitizeIsbn(String isbn) { return isbn == null ? null : isbn.replaceAll("[^0-9Xx]", "").toUpperCase(); }

    private String mapToInternal(List<String> rawTerms) {
        if (rawTerms == null || rawTerms.isEmpty()) return null;
        class Rule {
            final java.util.List<String> keys;
            final String name;
            Rule(java.util.List<String> k, String n){ this.keys=k; this.name=n; }
        }
    java.util.List<Rule> rules = java.util.List.of(
        new Rule(java.util.List.of("terror","horror"), "Terror"),
        new Rule(java.util.List.of("romance","love","romant"), "Romance"),
        new Rule(java.util.List.of("comics","graphic","hq","manga","mangá","mangas"), "HQ"),
        new Rule(java.util.List.of("fantasy","fantasia"), "Fantasia"),
        new Rule(java.util.List.of("science fiction","sci-fi","ficcao cientifica","ficção cientifica","ficcao"), "Ficção Científica"),
        new Rule(java.util.List.of("thriller","suspense"), "Suspense"),
        new Rule(java.util.List.of("mystery","misterio","mistério","policial","crime","detective"), "Policial"),
        new Rule(java.util.List.of("biography","biografia","memoir","memorias"), "Biografia"),
        new Rule(java.util.List.of("history","historia","história"), "História"),
        new Rule(java.util.List.of("self-help","autoajuda"), "Autoajuda")
    );
        for (String term : rawTerms) {
            String t = normalize(term);
            for (Rule r : rules) {
        for (String k : r.keys) { if (t.contains(k)) return r.name; }
            }
        }
        return null;
    }

    private String normalize(String s) {
        if (s == null) return "";
        var n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.toLowerCase(Locale.ROOT);
    }

    // no-op utility kept for potential reuse
}
