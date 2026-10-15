package br.ucsal.ted.relatorios.web;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.relatorios.aplicacao.RelatorioService;
import br.ucsal.ted.relatorios.aplicacao.dto.RelatorioColaboradorResponse;
import br.ucsal.ted.relatorios.aplicacao.dto.RelatorioDiscenteResponse;
import br.ucsal.ted.relatorios.aplicacao.dto.RelatorioDocentesResponse;
import br.ucsal.ted.relatorios.aplicacao.dto.RelatorioEstruturaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatórios", description = "RF008, RF009, RF016, RF018 e RNF028 - Relatórios com identificação do emissor")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @PostMapping("/estrutura")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "RF008 - Emite o relatório de IES, centros acadêmicos e cursos com suas vinculações")
    public RelatorioEstruturaResponse estrutura() {
        return service.emitirEstrutura();
    }

    @PostMapping("/docentes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "RF009 - Emite o relatório de docentes por centro acadêmico, separando ativos e inativos")
    public RelatorioDocentesResponse docentes(@RequestParam(required = false) Status status) {
        return service.emitirDocentes(status);
    }

    @PostMapping("/colaborador")
    @PreAuthorize("hasAnyRole('ADMIN', 'COLABORADOR')")
    @Operation(summary = "RF016 - Emite o relatório com as informações cadastradas pelo colaborador")
    public RelatorioColaboradorResponse colaborador() {
        return service.emitirColaborador();
    }

    @PostMapping("/discente")
    @PreAuthorize("hasRole('USUARIO')")
    @Operation(summary = "RF018 - Emite o relatório com as informações do discente e suas solicitações")
    public RelatorioDiscenteResponse discente() {
        return service.emitirDiscente();
    }
}
