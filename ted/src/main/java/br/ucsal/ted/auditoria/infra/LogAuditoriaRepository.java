package br.ucsal.ted.auditoria.infra;

import br.ucsal.ted.auditoria.dominio.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long>,
        JpaSpecificationExecutor<LogAuditoria> {
}
