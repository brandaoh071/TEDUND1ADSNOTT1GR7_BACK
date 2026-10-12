package br.ucsal.ted.auditoria.aplicacao;

import br.ucsal.ted.auditoria.dominio.LogAuditoria;
import br.ucsal.ted.auditoria.infra.LogAuditoriaRepository;
import br.ucsal.ted.compartilhado.auditoria.EventoAuditoria;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class RegistroDeAuditoria {

    private final LogAuditoriaRepository logs;

    public RegistroDeAuditoria(LogAuditoriaRepository logs) {
        this.logs = logs;
    }

    @ApplicationModuleListener
    public void registrar(EventoAuditoria evento) {
        logs.save(LogAuditoria.de(evento));
    }
}
