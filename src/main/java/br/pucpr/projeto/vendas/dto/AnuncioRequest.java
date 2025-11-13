package br.pucpr.projeto.vendas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record AnuncioRequest(
        @NotNull Long livroId,
        @NotNull @DecimalMin("0.0") BigDecimal preco,
        @NotNull @Min(1) Integer quantidade
) {}
