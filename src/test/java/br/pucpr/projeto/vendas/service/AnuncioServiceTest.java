package br.pucpr.projeto.vendas.service;

import br.pucpr.projeto.auth.model.User;
import br.pucpr.projeto.livros.model.Livro;
import br.pucpr.projeto.livros.repository.LivroRepository;
import br.pucpr.projeto.vendas.dto.AnuncioRequest;
import br.pucpr.projeto.vendas.model.Anuncio;
import br.pucpr.projeto.vendas.repository.AnuncioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AnuncioServiceTest {

    @Mock AnuncioRepository anuncioRepository;
    @Mock LivroRepository livroRepository;
    @InjectMocks AnuncioService service;

    private static void setId(Object entity, long id) throws Exception {
        var f = entity.getClass().getDeclaredField("id");
        f.setAccessible(true);
        f.set(entity, id);
    }

    private static User user(long id, String email) throws Exception {
        var u = new User("Teste", email, "hash");
        setId(u, id);
        return u;
    }

    @Test @DisplayName("comprar: zera estoque e marca VENDIDO quando quantidade fica 0")
    void comprarZeraEstoqueEVende() throws Exception {
        var vend = user(1, "vend@x");
        var comp = user(2, "comp@y");
    var livro = new Livro("T","A", new br.pucpr.projeto.livros.model.Categoria("C"), new BigDecimal("1.00"), "i"); setId(livro, 10);
        var an = new Anuncio(); setId(an, 100);
        an.setLivro(livro); an.setVendedor(vend); an.setPreco(new BigDecimal("9.90")); an.setQuantidade(1); an.setStatus(Anuncio.Status.ATIVO);

        when(anuncioRepository.findById(100L)).thenReturn(Optional.of(an));
        when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = service.comprar(comp, 100L, 1);
        assertEquals(0, res.getQuantidade());
        assertEquals(Anuncio.Status.VENDIDO, res.getStatus());
    }

    @Test @DisplayName("comprar: impede comprar o próprio anúncio (422)")
    void comprarProprioAnuncio422() throws Exception {
        var vend = user(1, "vend@x");
    var livro = new Livro("T","A", new br.pucpr.projeto.livros.model.Categoria("C"), new BigDecimal("1.00"), "i"); setId(livro, 10);
        var an = new Anuncio(); setId(an, 101);
        an.setLivro(livro); an.setVendedor(vend); an.setPreco(new BigDecimal("9.90")); an.setQuantidade(5); an.setStatus(Anuncio.Status.ATIVO);
        when(anuncioRepository.findById(101L)).thenReturn(Optional.of(an));

        var ex = assertThrows(ResponseStatusException.class, () -> service.comprar(vend, 101L, 1));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
    }

    @Test @DisplayName("comprar: conflito quando quantidade insuficiente (409)")
    void comprarQuantidadeInsuficiente409() throws Exception {
        var vend = user(1, "vend@x");
        var comp = user(2, "comp@y");
    var livro = new Livro("T","A", new br.pucpr.projeto.livros.model.Categoria("C"), new BigDecimal("1.00"), "i"); setId(livro, 10);
        var an = new Anuncio(); setId(an, 102);
        an.setLivro(livro); an.setVendedor(vend); an.setPreco(new BigDecimal("9.90")); an.setQuantidade(1); an.setStatus(Anuncio.Status.ATIVO);
        when(anuncioRepository.findById(102L)).thenReturn(Optional.of(an));

        var ex = assertThrows(ResponseStatusException.class, () -> service.comprar(comp, 102L, 2));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test @DisplayName("criar: 422 quando livro não existe")
    void criarLivroNaoExiste422() throws Exception {
        var vend = user(1, "v@x");
        var req = new AnuncioRequest(999L, new BigDecimal("12.34"), 2);
        when(livroRepository.findById(999L)).thenReturn(Optional.empty());
        var ex = assertThrows(ResponseStatusException.class, () -> service.criar(vend, req));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
    }
}
