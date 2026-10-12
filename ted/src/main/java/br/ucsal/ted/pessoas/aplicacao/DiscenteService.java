package br.ucsal.ted.pessoas.aplicacao;

import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;
import br.ucsal.ted.compartilhado.dominio.Especificacoes;
import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.compartilhado.excecao.RecursoNaoEncontradoException;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import br.ucsal.ted.compartilhado.web.PaginaResponse;
import br.ucsal.ted.estrutura.api.CursoResumo;
import br.ucsal.ted.estrutura.api.EstruturaApi;
import br.ucsal.ted.identidade.api.NomePerfil;
import br.ucsal.ted.identidade.api.UsuarioApi;
import br.ucsal.ted.pessoas.aplicacao.dto.DiscenteRequest;
import br.ucsal.ted.pessoas.aplicacao.dto.DiscenteResponse;
import br.ucsal.ted.pessoas.dominio.Discente;
import br.ucsal.ted.pessoas.infra.DiscenteRepository;
import br.ucsal.ted.pessoas.infra.HistoricoInativacaoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DiscenteService extends ServicoDePessoa<Discente> {

    private final EstruturaApi estrutura;

    public DiscenteService(DiscenteRepository discentes, HistoricoInativacaoRepository historicos,
                           UsuarioApi usuarioApi, EstruturaApi estrutura, ApplicationEventPublisher eventos) {
        super(discentes, historicos, usuarioApi, eventos);
        this.estrutura = estrutura;
    }

    @Override
    protected String nomeEntidade() {
        return "Discente";
    }

    @Override
    protected NomePerfil perfilDaConta() {
        return NomePerfil.USUARIO;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<DiscenteResponse> listar(Long cursoId, Status status, String busca, Pageable pageable) {
        Specification<Discente> filtro = Especificacoes.<Discente>igual("cursoId", cursoId)
                .and(Especificacoes.igual("status", status))
                .and(Especificacoes.contemEmAlgum(busca, "nomeCompleto", "matricula", "email"));
        Page<Discente> pagina = pessoas.findAll(filtro, pageable);
        Map<Long, CursoResumo> cursos = estrutura.buscarCursos(
                pagina.getContent().stream().map(Discente::getCursoId).collect(Collectors.toSet()));
        return PaginaResponse.de(pagina, d -> DiscenteResponse.de(d, cursos.get(d.getCursoId())));
    }

    @Transactional(readOnly = true)
    public DiscenteResponse buscar(Long id) {
        return resposta(buscarEntidade(id));
    }

    @Transactional
    public DiscenteResponse cadastrar(DiscenteRequest request) {
        cursoAtivo(request.cursoId());
        Long usuarioId = criarConta(request.matricula(), request.email(), request.senha());
        Discente discente = new Discente(request.matricula(), request.nomeCompleto(), request.email(),
                request.telefone(), request.cursoId(), usuarioId);
        pessoas.save(discente);
        registrar(AcaoAuditada.CRIAR, discente, discente.getMatricula());
        return resposta(discente);
    }

    @Transactional
    public DiscenteResponse editar(Long id, DiscenteRequest request) {
        Discente discente = buscarEntidade(id);
        validarEdicao(discente, request.matricula(), request.email());
        if (!discente.getCursoId().equals(request.cursoId())) {
            cursoAtivo(request.cursoId());
        }
        discente.atualizar(request.nomeCompleto(), request.email(), request.telefone(), request.cursoId());
        registrar(AcaoAuditada.EDITAR, discente, discente.getMatricula());
        return resposta(discente);
    }

    @Override
    protected void antesDeReativar(Discente discente) {
        cursoAtivo(discente.getCursoId());
    }

    private CursoResumo cursoAtivo(Long id) {
        CursoResumo curso = estrutura.buscarCurso(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso", id));
        if (!curso.ativo()) {
            throw new RegraNegocioException("O curso " + curso.sigla() + " está inativo");
        }
        return curso;
    }

    private DiscenteResponse resposta(Discente discente) {
        return DiscenteResponse.de(discente, estrutura.buscarCurso(discente.getCursoId()).orElse(null));
    }
}
