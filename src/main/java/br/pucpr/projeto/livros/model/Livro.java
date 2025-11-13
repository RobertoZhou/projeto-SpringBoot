package br.pucpr.projeto.livros.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "livros", uniqueConstraints = {
    @UniqueConstraint(name = "uk_livros_isbn", columnNames = "isbn")
})
public class Livro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 120)
    private String autor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Categoria categoria;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal preco;

    @Column(length = 13, unique = true)
    private String isbn;

    @Column(name = "imagem_capa_url", length = 600)
    private String imagemCapaUrl;

    protected Livro() {}

    public Livro(String titulo, String autor, Categoria categoria, BigDecimal preco, String isbn) {
        this.titulo = titulo; this.autor = autor; this.categoria = categoria; this.preco = preco; this.isbn = isbn;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public Categoria getCategoria() { return categoria; }
    public BigDecimal getPreco() { return preco; }
    public String getIsbn() { return isbn; }
    public String getImagemCapaUrl() { return imagemCapaUrl; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setAutor(String autor) { this.autor = autor; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setImagemCapaUrl(String imagemCapaUrl) { this.imagemCapaUrl = imagemCapaUrl; }
}
