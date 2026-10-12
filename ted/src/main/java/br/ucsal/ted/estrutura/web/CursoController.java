package br.ucsal.ted.estrutura.web;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.compartilhado.web.PaginaResponse;
import br.ucsal.ted.estrutura.aplicacao.CursoService;
import br.ucsal.ted.estrutura.aplicacao.dto.CursoRequest;
import br.ucsal.ted.estrutura.aplicacao.dto.CursoResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cursos")
@Tag(name = "Cursos", description = "RF004")
public class CursoController {

    private final CursoService service;

    public CursoController(CursoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os cursos com filtro por centro acadêmico, status e busca")
    public PaginaResponse<CursoResponse> listar(@RequestParam(required = false) Long centroAcademicoId,
                                                @RequestParam(required = false) Status status,
                                              @RequestParam(required = false) String busca,
                                              @PageableDefault(size = 20, sort = "sigla") Pageable pageable) {
        return service.listar(centroAcademicoId, status, busca, pageable);
    }

    @GetMapping("/{id}")
    public CursoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CursoResponse cadastrar(@Valid @RequestBody CursoRequest request) {
        return service.cadastrar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CursoResponse editar(@PathVariable Long id, @Valid @RequestBody CursoRequest request) {
        return service.editar(id, request);
    }

    @PatchMapping("/{id}/inativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void inativar(@PathVariable Long id) {
        service.inativar(id);
    }

    @PatchMapping("/{id}/reativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void reativar(@PathVariable Long id) {
        service.reativar(id);
    }
}
