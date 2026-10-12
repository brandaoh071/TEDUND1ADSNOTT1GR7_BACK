package br.ucsal.ted.pessoas.aplicacao.dto;

import br.ucsal.ted.pessoas.dominio.HistoricoInativacao;
import br.ucsal.ted.pessoas.dominio.MotivoInativacao;

import java.time.Instant;

public record HistoricoInativacaoResponse(
        Long id,
        MotivoInativacao motivo,
        String descricao,
        Instant dataHora,
        String registradoPor) {

    public static HistoricoInativacaoResponse de(HistoricoInativacao historico) {
        return new HistoricoInativacaoResponse(historico.getId(), historico.getMotivo(), historico.getDescricao(),
                historico.getDataHora(), historico.getRegistradoPor());
    }
}
