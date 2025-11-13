package br.pucpr.projeto.livros.repository;

import br.pucpr.projeto.livros.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LivroRepository extends JpaRepository<Livro, Long> {
	Optional<Livro> findByIsbn(String isbn);
    boolean existsByIsbn(String isbn);
}
