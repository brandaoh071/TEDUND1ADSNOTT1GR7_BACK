package br.ucsal.ted.solicitacoes.aplicacao.dto;

import jakarta.validation.constraints.Size;

public record ListaEsperaRequest(@Size(max = 500) String observacao) {
}
