package br.pucpr.projeto.vendas.model;

import br.pucpr.projeto.auth.model.User;
import br.pucpr.projeto.livros.model.Livro;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "anuncios")
public class Anuncio {
    public enum Status { ATIVO, PAUSADO, VENDIDO }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Livro livro;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User vendedor;

    @DecimalMin("0.0")
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ATIVO;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    @PrePersist
    public void prePersist(){
        criadoEm = LocalDateTime.now();
        atualizadoEm = criadoEm;
    }
    @PreUpdate
    public void preUpdate(){ atualizadoEm = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
    public User getVendedor() { return vendedor; }
    public void setVendedor(User vendedor) { this.vendedor = vendedor; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
