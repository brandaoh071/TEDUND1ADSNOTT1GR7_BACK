package br.ucsal.ted.solicitacoes.web;

import br.ucsal.ted.compartilhado.web.PaginaResponse;
import br.ucsal.ted.solicitacoes.aplicacao.SolicitacaoService;
import br.ucsal.ted.solicitacoes.aplicacao.dto.ListaEsperaRequest;
import br.ucsal.ted.solicitacoes.aplicacao.dto.SemestreResponse;
import br.ucsal.ted.solicitacoes.aplicacao.dto.SolicitacaoRequest;
import br.ucsal.ted.solicitacoes.aplicacao.dto.SolicitacaoResponse;
import br.ucsal.ted.solicitacoes.dominio.StatusSolicitacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/solicitacoes")
@Tag(name = "Solicitações", description = "RF013, RF015 e RF017 - Solicitação e validação de disciplinas")
public class SolicitacaoController {

    private final SolicitacaoService service;

    public SolicitacaoController(SolicitacaoService service) {
        this.service = service;
    }

    @GetMapping("/semestre")
    @PreAuthorize("hasRole('USUARIO')")
    @Operation(summary = "Semestre de referência da próxima solicitação e se o discente já solicitou")
    public SemestreResponse semestre() {
        return service.semestreAtual();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USUARIO')")
    @Operation(summary = "Discente solicita de 2 a 9 disciplinas com prioridade de 1 a 5 (no máximo 2 com prioridade 5)")
    public SolicitacaoResponse enviar(@Valid @RequestBody SolicitacaoRequest request) {
        return service.enviar(request);
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasRole('USUARIO')")
    public PaginaResponse<SolicitacaoResponse> minhas(@PageableDefault(size = 20) Pageable pageable) {
        return service.minhas(pageable);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COLABORADOR')")
    @Operation(summary = "Fila de solicitações por ordem de recebimento; o colaborador vê as do seu centro acadêmico")
    public PaginaResponse<SolicitacaoResponse> fila(@RequestParam(required = false) StatusSolicitacao status,
                                                    @RequestParam(required = false) String semestre,
                                                    @PageableDefault(size = 20) Pageable pageable) {
        return service.fila(status, semestre, pageable);
    }

    @GetMapping("/{id}")
    public SolicitacaoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PatchMapping("/{id}/aprovar")
    @PreAuthorize("hasAnyRole('ADMIN', 'COLABORADOR')")
    @Operation(summary = "Aprova a solicitação mais antiga da fila; o status passa para MATRICULADA")
    public SolicitacaoResponse aprovar(@PathVariable Long id) {
        return service.aprovar(id);
    }

    @PatchMapping("/{id}/lista-espera")
    @PreAuthorize("hasAnyRole('ADMIN', 'COLABORADOR')")
    @Operation(summary = "Envia a solicitação mais antiga da fila para a LISTA_ESPERA")
    public SolicitacaoResponse enviarParaListaDeEspera(@PathVariable Long id,
                                                       @Valid @RequestBody(required = false) ListaEsperaRequest request) {
        return service.enviarParaListaDeEspera(id, request == null ? null : request.observacao());
    }
}
