package br.ucsal.ted.pessoas.web;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.compartilhado.web.PaginaResponse;
import br.ucsal.ted.pessoas.aplicacao.DiscenteService;
import br.ucsal.ted.pessoas.aplicacao.dto.DiscenteRequest;
import br.ucsal.ted.pessoas.aplicacao.dto.DiscenteResponse;
import br.ucsal.ted.pessoas.aplicacao.dto.HistoricoInativacaoResponse;
import br.ucsal.ted.pessoas.aplicacao.dto.InativacaoRequest;
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

import java.util.List;

@RestController
@RequestMapping("/api/discentes")
@PreAuthorize("hasAnyRole('ADMIN', 'COLABORADOR')")
@Tag(name = "Discentes", description = "RF011 e RF012 - Discentes com perfil Usuário")
public class DiscenteController {

    private final DiscenteService service;

    public DiscenteController(DiscenteService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os discentes com filtro por curso, status e busca")
    public PaginaResponse<DiscenteResponse> listar(@RequestParam(required = false) Long cursoId,
                                                  @RequestParam(required = false) Status status,
                                                  @RequestParam(required = false) String busca,
                                                  @PageableDefault(size = 20, sort = "nomeCompleto") Pageable pageable) {
        return service.listar(cursoId, status, busca, pageable);
    }

    @GetMapping("/{id}")
    public DiscenteResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra o discente e cria a conta de acesso com perfil USUARIO")
    public DiscenteResponse cadastrar(@Valid @RequestBody DiscenteRequest request) {
        return service.cadastrar(request);
    }

    @PutMapping("/{id}")
    public DiscenteResponse editar(@PathVariable Long id, @Valid @RequestBody DiscenteRequest request) {
        return service.editar(id, request);
    }

    @PatchMapping("/{id}/inativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Inativa o discente registrando o motivo no histórico e desativa a conta de acesso")
    public void inativar(@PathVariable Long id, @Valid @RequestBody InativacaoRequest request) {
        service.inativar(id, request.motivo(), request.descricao());
    }

    @PatchMapping("/{id}/reativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reativar(@PathVariable Long id) {
        service.reativar(id);
    }

    @GetMapping("/{id}/historico")
    public List<HistoricoInativacaoResponse> historico(@PathVariable Long id) {
        return service.historico(id);
    }
}
