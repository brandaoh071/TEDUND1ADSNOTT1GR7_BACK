package br.ucsal.ted.solicitacoes.api;

import java.time.Instant;
import java.util.List;

public record SolicitacaoFicha(Long id, Long discenteId, String semestreReferencia, Instant dataSolicitacao,
                               String status, String validadoPor, Instant dataValidacao, String observacao,
                               List<ItemFicha> itens) {

    public record ItemFicha(Long disciplinaId, int prioridade) {
    }
}
