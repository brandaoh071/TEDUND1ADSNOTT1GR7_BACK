package br.ucsal.ted.solicitacoes.aplicacao.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SolicitacaoRequest(@NotNull @Valid List<ItemSolicitacaoRequest> itens) {
}
