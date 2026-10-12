package br.ucsal.ted.compartilhado.auditoria;

import br.ucsal.ted.compartilhado.seguranca.UsuarioLogado;

import java.time.Instant;

public record EventoAuditoria(
        String usuario,
        String perfis,
        AcaoAuditada acao,
        String entidade,
        Long idRegistro,
        String detalhes,
        Instant dataHora) {

    public static EventoAuditoria de(AcaoAuditada acao, String entidade, Long idRegistro, String detalhes) {
        UsuarioLogado usuario = UsuarioLogado.atual();
        return new EventoAuditoria(usuario.login(), usuario.perfisComoTexto(), acao, entidade, idRegistro,
                detalhes, Instant.now());
    }

    public static EventoAuditoria deAcesso(AcaoAuditada acao, String login, String perfis, Long idUsuario,
                                           String detalhes) {
        return new EventoAuditoria(login, perfis, acao, "Usuario", idUsuario, detalhes, Instant.now());
    }
}
