package br.ucsal.ted.solicitacoes.aplicacao.dto;

import jakarta.validation.constraints.NotNull;

public record ItemSolicitacaoRequest(@NotNull Long disciplinaId, @NotNull Integer prioridade) {
}
