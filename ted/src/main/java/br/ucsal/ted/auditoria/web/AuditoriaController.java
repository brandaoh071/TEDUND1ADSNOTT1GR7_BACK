package br.ucsal.ted.auditoria.web;

import br.ucsal.ted.auditoria.aplicacao.ConsultaAuditoriaService;
import br.ucsal.ted.auditoria.aplicacao.LogAuditoriaResponse;
import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;
import br.ucsal.ted.compartilhado.web.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Auditoria", description = "RF007 - Logs de todas as ações do sistema")
public class AuditoriaController {

    private final ConsultaAuditoriaService service;

    public AuditoriaController(ConsultaAuditoriaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os logs de auditoria com filtros; datas em UTC (ISO-8601)")
    public PaginaResponse<LogAuditoriaResponse> listar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) AcaoAuditada acao,
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant ate,
            @PageableDefault(size = 20, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(usuario, acao, entidade, de, ate, pageable);
    }
}
