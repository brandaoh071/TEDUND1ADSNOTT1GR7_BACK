package br.ucsal.ted.disciplinas.web;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.compartilhado.web.PaginaResponse;
import br.ucsal.ted.disciplinas.aplicacao.DisciplinaService;
import br.ucsal.ted.disciplinas.aplicacao.dto.DisciplinaRequest;
import br.ucsal.ted.disciplinas.aplicacao.dto.DisciplinaResponse;
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
@RequestMapping("/api/disciplinas")
@Tag(name = "Disciplinas", description = "RF010 - Disciplinas lecionadas pelos colaboradores")
public class DisciplinaController {

    private final DisciplinaService service;

    public DisciplinaController(DisciplinaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista as disciplinas com filtro por curso e status; minhas=true traz só as do docente logado")
    public PaginaResponse<DisciplinaResponse> listar(@RequestParam(required = false) Long cursoId,
                                                     @RequestParam(required = false) Status status,
                                                     @RequestParam(required = false) String busca,
                                                     @RequestParam(defaultValue = "false") boolean minhas,
                                                     @PageableDefault(size = 20, sort = "sigla") Pageable pageable) {
        return service.listar(cursoId, status, busca, minhas, pageable);
    }

    @GetMapping("/{id}")
    public DisciplinaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'COLABORADOR')")
    public DisciplinaResponse cadastrar(@Valid @RequestBody DisciplinaRequest request) {
        return service.cadastrar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COLABORADOR')")
    public DisciplinaResponse editar(@PathVariable Long id, @Valid @RequestBody DisciplinaRequest request) {
        return service.editar(id, request);
    }

    @PatchMapping("/{id}/inativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'COLABORADOR')")
    public void inativar(@PathVariable Long id) {
        service.inativar(id);
    }

    @PatchMapping("/{id}/reativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'COLABORADOR')")
    public void reativar(@PathVariable Long id) {
        service.reativar(id);
    }
}
