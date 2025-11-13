package br.pucpr.projeto.vendas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CompraRequest(
        @NotNull @Min(1) Integer quantidade
) {}
