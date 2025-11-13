package br.pucpr.projeto.vendas.service;

import br.pucpr.projeto.auth.model.User;
import br.pucpr.projeto.livros.model.Livro;
import br.pucpr.projeto.livros.repository.LivroRepository;
import br.pucpr.projeto.vendas.dto.AnuncioRequest;
import br.pucpr.projeto.vendas.model.Anuncio;
import br.pucpr.projeto.vendas.repository.AnuncioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class AnuncioService {
    private final AnuncioRepository anuncioRepository;
    private final LivroRepository livroRepository;
    private static final String MSG_ANUNCIO_NAO_ENCONTRADO = "Anúncio não encontrado";
    private static final String MSG_ID_OBRIGATORIO = "id é obrigatório";

    public AnuncioService(AnuncioRepository anuncioRepository, LivroRepository livroRepository) {
        this.anuncioRepository = anuncioRepository;
        this.livroRepository = livroRepository;
    }

    public List<Anuncio> listarPublicos(){
        return anuncioRepository.findByStatus(Anuncio.Status.ATIVO);
    }

    public List<Anuncio> listarDoVendedor(User vendedor){
        return anuncioRepository.findByVendedor(vendedor);
    }

    public Anuncio criar(User vendedor, AnuncioRequest req){
    Long livroId = Objects.requireNonNull(req.livroId(), "livroId é obrigatório");
    Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Livro não encontrado"));

        Anuncio a = new Anuncio();
        a.setLivro(livro);
        a.setVendedor(vendedor);
        a.setPreco(req.preco());
        a.setQuantidade(req.quantidade());
        a.setStatus(Anuncio.Status.ATIVO);
        return anuncioRepository.save(a);
    }

    public void alterarStatus(User vendedor, Long id, Anuncio.Status status){
    Long anuncioId = Objects.requireNonNull(id, MSG_ID_OBRIGATORIO);
    Anuncio a = anuncioRepository.findById(anuncioId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_ANUNCIO_NAO_ENCONTRADO));
        if (!a.getVendedor().getId().equals(vendedor.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não é o dono do anúncio");
        }
        a.setStatus(status);
        anuncioRepository.save(a);
    }

    public void deletar(User vendedor, Long id){
    Long anuncioId = Objects.requireNonNull(id, MSG_ID_OBRIGATORIO);
    Anuncio a = anuncioRepository.findById(anuncioId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_ANUNCIO_NAO_ENCONTRADO));
        if (!a.getVendedor().getId().equals(vendedor.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não é o dono do anúncio");
        }
        anuncioRepository.delete(a);
    }

    public Anuncio comprar(User comprador, Long id, int quantidade){
        if (quantidade < 1) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Quantidade inválida");
    Long anuncioId = Objects.requireNonNull(id, MSG_ID_OBRIGATORIO);
    Anuncio a = anuncioRepository.findById(anuncioId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_ANUNCIO_NAO_ENCONTRADO));
        if (a.getStatus() != Anuncio.Status.ATIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Anúncio indisponível");
        }
        if (a.getQuantidade() < quantidade){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Quantidade indisponível");
        }
        // Evitar comprar o próprio anúncio
        if (a.getVendedor().getId().equals(comprador.getId())){
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Você não pode comprar seu próprio anúncio");
        }

        a.setQuantidade(a.getQuantidade() - quantidade);
        if (a.getQuantidade() == 0){
            a.setStatus(Anuncio.Status.VENDIDO);
        }
        return anuncioRepository.save(a);
    }
}
