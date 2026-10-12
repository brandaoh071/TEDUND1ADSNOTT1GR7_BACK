package br.ucsal.ted.auditoria.aplicacao;

import br.ucsal.ted.auditoria.dominio.LogAuditoria;
import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;

import java.time.Instant;

public record LogAuditoriaResponse(
        Long id,
        String usuario,
        String perfis,
        AcaoAuditada acao,
        String entidade,
        Long idRegistro,
        String detalhes,
        Instant dataHora) {

    public static LogAuditoriaResponse de(LogAuditoria log) {
        return new LogAuditoriaResponse(log.getId(), log.getUsuario(), log.getPerfis(), log.getAcao(),
                log.getEntidade(), log.getIdRegistro(), log.getDetalhes(), log.getDataHora());
    }
}
