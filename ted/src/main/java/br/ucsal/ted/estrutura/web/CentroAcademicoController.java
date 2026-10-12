package br.ucsal.ted.estrutura.web;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.compartilhado.web.PaginaResponse;
import br.ucsal.ted.estrutura.aplicacao.CentroAcademicoService;
import br.ucsal.ted.estrutura.aplicacao.dto.CentroAcademicoRequest;
import br.ucsal.ted.estrutura.aplicacao.dto.CentroAcademicoResponse;
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
@RequestMapping("/api/centros-academicos")
@Tag(name = "Centros Acadêmicos", description = "RF003")
public class CentroAcademicoController {

    private final CentroAcademicoService service;

    public CentroAcademicoController(CentroAcademicoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os centros acadêmicos com filtro por IES, status e busca")
    public PaginaResponse<CentroAcademicoResponse> listar(@RequestParam(required = false) Long iesId,
                                                          @RequestParam(required = false) Status status,
                                              @RequestParam(required = false) String busca,
                                              @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.listar(iesId, status, busca, pageable);
    }

    @GetMapping("/{id}")
    public CentroAcademicoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CentroAcademicoResponse cadastrar(@Valid @RequestBody CentroAcademicoRequest request) {
        return service.cadastrar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CentroAcademicoResponse editar(@PathVariable Long id, @Valid @RequestBody CentroAcademicoRequest request) {
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
