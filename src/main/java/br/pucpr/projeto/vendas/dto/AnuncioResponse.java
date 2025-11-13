package br.pucpr.projeto.vendas.dto;

import br.pucpr.projeto.vendas.model.Anuncio;
import java.math.BigDecimal;

public record AnuncioResponse(
        Long id,
        Long livroId,
        String titulo,
        String autor,
        String imagemCapaUrl,
        BigDecimal preco,
        Integer quantidade,
        String status,
        Long vendedorId,
    String vendedorEmail,
    String vendedorNome
) {
    public static AnuncioResponse of(Anuncio a){
        return new AnuncioResponse(
                a.getId(),
                a.getLivro().getId(),
                a.getLivro().getTitulo(),
                a.getLivro().getAutor(),
                a.getLivro().getImagemCapaUrl(),
                a.getPreco(),
                a.getQuantidade(),
                a.getStatus().name(),
                a.getVendedor().getId(),
        a.getVendedor().getEmail(),
        a.getVendedor().getNome()
        );
    }
}
