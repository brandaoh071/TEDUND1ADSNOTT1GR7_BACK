package br.ucsal.ted.relatorios.aplicacao.dto;

import java.time.Instant;
import java.util.List;

public record SolicitacaoDoRelatorio(
        Long id,
        String discenteMatricula,
        String discenteNome,
        String semestreReferencia,
        Instant dataSolicitacao,
        String status,
        String validadoPor,
        Instant dataValidacao,
        String observacao,
        List<ItemDoRelatorio> itens) {

    public record ItemDoRelatorio(String sigla, String descricao, Integer cargaHoraria, int prioridade) {
    }
}
