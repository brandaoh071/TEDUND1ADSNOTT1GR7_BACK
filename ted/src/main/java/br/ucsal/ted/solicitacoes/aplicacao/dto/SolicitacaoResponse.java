package br.ucsal.ted.solicitacoes.aplicacao.dto;

import br.ucsal.ted.solicitacoes.dominio.StatusSolicitacao;

import java.time.Instant;
import java.util.List;

public record SolicitacaoResponse(
        Long id,
        Long discenteId,
        String discenteMatricula,
        String discenteNome,
        Long cursoId,
        String cursoSigla,
        String semestreReferencia,
        Instant dataSolicitacao,
        StatusSolicitacao status,
        List<ItemSolicitacaoResponse> itens,
        String validadoPor,
        Instant dataValidacao,
        String observacao,
        boolean proximaDaFila) {
}
