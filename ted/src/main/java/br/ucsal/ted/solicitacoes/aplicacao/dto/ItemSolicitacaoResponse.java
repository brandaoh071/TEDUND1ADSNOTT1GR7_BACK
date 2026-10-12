package br.ucsal.ted.solicitacoes.aplicacao.dto;

public record ItemSolicitacaoResponse(
        Long disciplinaId,
        String sigla,
        String descricao,
        Integer cargaHoraria,
        int prioridade) {
}
