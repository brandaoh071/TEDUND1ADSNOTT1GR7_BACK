package br.ucsal.ted.identidade.aplicacao;

import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;
import br.ucsal.ted.compartilhado.auditoria.EventoAuditoria;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegistroDeAcesso {

    private final ApplicationEventPublisher eventos;

    public RegistroDeAcesso(ApplicationEventPublisher eventos) {
        this.eventos = eventos;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sucesso(Long usuarioId, String login, String perfis) {
        eventos.publishEvent(EventoAuditoria.deAcesso(AcaoAuditada.LOGIN, login, perfis, usuarioId,
                "Login realizado"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void falha(String login) {
        eventos.publishEvent(EventoAuditoria.deAcesso(AcaoAuditada.LOGIN_FALHO, login, "", null,
                "Tentativa de login com credenciais inválidas"));
    }
}
