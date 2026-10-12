package br.ucsal.ted.auditoria.aplicacao;

import br.ucsal.ted.auditoria.dominio.LogAuditoria;
import br.ucsal.ted.auditoria.infra.LogAuditoriaRepository;
import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;
import br.ucsal.ted.compartilhado.dominio.Especificacoes;
import br.ucsal.ted.compartilhado.web.PaginaResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ConsultaAuditoriaService {

    private final LogAuditoriaRepository logs;

    public ConsultaAuditoriaService(LogAuditoriaRepository logs) {
        this.logs = logs;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<LogAuditoriaResponse> listar(String usuario, AcaoAuditada acao, String entidade,
                                                       Instant de, Instant ate, Pageable pageable) {
        Specification<LogAuditoria> filtro = Especificacoes.<LogAuditoria>contem("usuario", usuario)
                .and(Especificacoes.igual("acao", acao))
                .and(Especificacoes.igual("entidade", entidade == null || entidade.isBlank() ? null : entidade))
                .and(Especificacoes.aPartirDe("dataHora", de))
                .and(Especificacoes.ate("dataHora", ate));
        return PaginaResponse.de(logs.findAll(filtro, pageable), LogAuditoriaResponse::de);
    }
}
