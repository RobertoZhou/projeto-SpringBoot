package br.pucpr.projeto.vendas.repository;

import br.pucpr.projeto.vendas.model.Anuncio;
import br.pucpr.projeto.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {
    List<Anuncio> findByStatus(Anuncio.Status status);
    List<Anuncio> findByVendedor(User vendedor);
}
